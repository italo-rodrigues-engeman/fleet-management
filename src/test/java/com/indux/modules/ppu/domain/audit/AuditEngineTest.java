package com.indux.modules.ppu.domain.audit;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.ppu.ServiceType;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import com.indux.modules.ppu.domain.entities.rdo.EquipmentChecker;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOEquipment;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditBatchContext;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditContext;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditRDORow;
import com.indux.modules.ppu.domain.entities.rdo.audit.SAMCRow;
import com.indux.modules.ppu.domain.services.bm.audit.AuditEngine;
import com.indux.modules.ppu.domain.services.bm.audit.PlatformAliasResolver;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

class AuditEngineTest {
        private static final Set<String> ALLOWED_STATUSES = Set.of("APROVADO", "FINALIZADO");

        @Mock
        private OvertimeProjectionService overtimeProjectionService;
        @Mock
        private PlatformAliasResolver platformResolver;

        @InjectMocks
        AuditEngine auditEngine;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);

            lenient().when(platformResolver.toSigla(any()))
                    .thenAnswer(inv -> {
                        String platform = inv.getArgument(0);
                        return (platform == null || platform.isBlank()) ? "" : platform;
                    });

            lenient().when(platformResolver.toSigla(any(), any()))
                    .thenAnswer(inv -> {
                        String platform = inv.getArgument(0);
                        return (platform == null || platform.isBlank()) ? "" : platform;
                    });

            lenient().when(platformResolver.toCanonical(any()))
                    .thenAnswer(inv -> {
                        String platform = inv.getArgument(0);
                        if (platform == null || platform.isBlank()) return "";
                        return platform.replaceAll("[^A-Z0-9]", "").toUpperCase();
                    });

            lenient().when(platformResolver.toCanonical(any(), any()))
                    .thenAnswer(inv -> {
                        String platform = inv.getArgument(0);
                        if (platform == null || platform.isBlank()) return "";
                        return platform.replaceAll("[^A-Z0-9]", "").toUpperCase();
                    });

            lenient().when(overtimeProjectionService.project(any(), any(), any()))
                    .thenAnswer(inv -> {
                        List<RDOServiceEntity> base = inv.getArgument(0);
                        if (base == null || base.isEmpty()) {
                            return new OvertimeProjectionService.ProjectionResult(List.of(), List.of());
                        }
                        
                        List<RDOServiceEntity> working = base.stream()
                                .map(RDOServiceEntity::copy)
                                .collect(Collectors.toCollection(ArrayList::new));
                        
                        return new OvertimeProjectionService.ProjectionResult(working, List.of());
                    });
        }

        @Test
        @DisplayName("Should don't return anything divergence")
        void shouldDontReturnAnythingDivergence() {
                var samcItems = List.of(AuditEngineFixture.samcRow("1"), AuditEngineFixture.samcRowOvertime("5"),
                                AuditEngineFixture.samcRowFulltime("0.5"), AuditEngineFixture.samcRowEquipment("2"));
                var serviceWithOvertime = AuditEngineFixture.serviceEntity(Duration.ofHours(5));
                var rdo = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(5), 2);
                var ctx = new AuditContext(rdo.getDate(), rdo.getPlatform(), AuditEngineFixture.ppuEntityAuditEngine(),
                                rdo, samcItems);

                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenAnswer(AuditEngineFixture.byOvertimeTotals(
                                                "overtime-id",
                                                "1.1.1.2.5",
                                                "Hora extraordinária de Supervisão"));

                var result = auditEngine.audit(ctx);
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return divergence by SAMC (quantity and overtimes)")
        void shouldReturnDivergencesBySAMC() {
                var samcItems = List.of(AuditEngineFixture.samcRow("2"), AuditEngineFixture.samcRowOvertime("0"),
                                AuditEngineFixture.samcRowFulltime("0.5"), AuditEngineFixture.samcRowEquipment("2"));
                var rdo = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(0), 2);
                var ctx = new AuditContext(rdo.getDate(), rdo.getPlatform(), AuditEngineFixture.ppuEntityAuditEngine(),
                                rdo, samcItems);

                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenReturn(new OvertimeProjectionService.ProjectionResult(
                                                List.of(AuditEngineFixture.serviceEntity(Duration.ofHours(0)),
                                                                AuditEngineFixture.serviceEntityFulltime()),
                                                List.of()));

                var result = auditEngine.audit(ctx);
                assertTrue(result.stream().anyMatch((n) -> n.text().contains("quantidade")));
                assertEquals(1, result.size());
                var samcItemsWithHours = List.of(AuditEngineFixture.samcRow("2"),
                                AuditEngineFixture.samcRowOvertime("2"), AuditEngineFixture.samcRowFulltime("0.5"),
                                AuditEngineFixture.samcRowEquipment("2"));
                var newContext = new AuditContext(rdo.getDate(), rdo.getPlatform(),
                                AuditEngineFixture.ppuEntityAuditEngine(), rdo, samcItemsWithHours);

                result = auditEngine.audit(newContext);
                assertEquals(2, result.size());
                assertTrue(result.stream().anyMatch((n) -> n.line().contains("Hora extraordinária")));
        }

        @Test
        @DisplayName("Should return divergence by RDO (line missing, quantity, overtimes)")
        void shouldReturnDivergencesByRDO() {
                var samcItems = List.of(AuditEngineFixture.samcRowOvertime("0"),
                                AuditEngineFixture.samcRowFulltime("0.5"), AuditEngineFixture.samcRowEquipment("2"));
                var rdo = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(0), 2);
                var serviceWithOvertime = AuditEngineFixture.serviceEntity(Duration.ofHours(0));
                var ctx = new AuditContext(rdo.getDate(), rdo.getPlatform(), AuditEngineFixture.ppuEntityAuditEngine(),
                                rdo, samcItems);
                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenReturn(new OvertimeProjectionService.ProjectionResult(
                                                List.of(serviceWithOvertime,
                                                                AuditEngineFixture.serviceEntityFulltime()),
                                                List.of()));
                var result = auditEngine.audit(ctx);
                assertTrue(result.stream().anyMatch((n) -> n.text().contains("Linha ausente")));
                assertEquals(1, result.size());

                var samcItemsTwo = List.of(AuditEngineFixture.samcRow("3"), AuditEngineFixture.samcRowAssistant("2"),
                                AuditEngineFixture.samcRowOvertime("2"), AuditEngineFixture.samcRowFulltime("0.5"),
                                AuditEngineFixture.samcRowEquipment("2"));
                var newContext = new AuditContext(rdo.getDate(), rdo.getPlatform(),
                                AuditEngineFixture.ppuEntityAuditEngine(), rdo, samcItemsTwo);

                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenReturn(new OvertimeProjectionService.ProjectionResult(
                                                List.of(serviceWithOvertime,
                                                                AuditEngineFixture.serviceEntityFulltime()),
                                                List.of()));

                result = auditEngine.audit(newContext);
                assertEquals(3, result.size());
                assertTrue(result.stream().anyMatch((n) -> n.line().contains("Hora extraordinária")));
                assertTrue(result.stream().anyMatch((n) -> n.text().contains("quantidade")));
                assertTrue(result.stream().anyMatch((n) -> n.text().contains("Linha ausente")));

                samcItems = List.of(AuditEngineFixture.samcRow("1"), AuditEngineFixture.samcRowOvertime("5"),
                                AuditEngineFixture.samcRowFulltime("2"), AuditEngineFixture.samcRowEquipment("2"));
                rdo = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(5), 2);
                ctx = new AuditContext(rdo.getDate(), rdo.getPlatform(), AuditEngineFixture.ppuEntityAuditEngine(), rdo,
                                samcItems);
                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenAnswer(AuditEngineFixture.byOvertimeTotals(
                                                "overtime-id",
                                                "1.1.1.2.5",
                                                "Hora extraordinária de Supervisão"));
                result = auditEngine.audit(ctx);
                assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return divergence in Headers")
        void shouldVerifyHeaders() {
                var samcItems = List.of(AuditEngineFixture.samcRow("2"));
                var rdo = RDOEntity.builder().contract(Map.of("codeSap", "10")).date(LocalDate.now())
                                .platform("Plataforma").build();
                var ctx = new AuditContext(rdo.getDate(), rdo.getPlatform(), AuditEngineFixture.ppuEntityAuditEngine(),
                                rdo, samcItems);

                var result = auditEngine.audit(ctx);
                assertTrue(result.stream().anyMatch((n) -> n.text().contains("Contrato")));
                assertTrue(result.stream().anyMatch((n) -> n.text().contains("Datas")));
                assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Bulk Audit - Should not return divergences when SAMC and RDO are consistent")
        void shouldNotReturnDivergencesInBulkAudit() {
                var ppu = AuditEngineFixture.ppuEntityAuditEngine();
                var rdo = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(5), 2);

                var samcItems = List.of(
                                AuditEngineFixture.samcRow("1"),
                                AuditEngineFixture.samcRowOvertime("5"),
                                AuditEngineFixture.samcRowFulltime("0.5"),
                                AuditEngineFixture.samcRowEquipment("2"));

                var ctx = new AuditBatchContext(
                                rdo.getDate(),
                                rdo.getDate(),
                                ppu,
                                List.of(rdo),
                                samcItems,
                                Map.of(),
                                Map.of());

                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenAnswer(inv -> {
                                        List<RDOServiceEntity> base = inv.getArgument(0);

                                        double hours = base.stream()
                                                        .filter(Objects::nonNull)
                                                        .map(RDOServiceEntity::getOvertimeHourTotais)
                                                        .filter(Objects::nonNull)
                                                        .mapToDouble(d -> d.toSeconds() / 3600.0)
                                                        .sum();

                                        List<RDOServiceEntity> working = base.stream()
                                                        .map(RDOServiceEntity::copy)
                                                        .collect(Collectors.toCollection(ArrayList::new));

                                        List<RDOServiceEntity> virtuals = new ArrayList<>();

                                        if (hours > 0.0001) {
                                                var ot = RDOServiceEntity.builder()
                                                                .serviceID("overtime-id")
                                                                .serviceNumber("1.1.1.2.5")
                                                                .serviceName("Hora extraordinária de Supervisão")
                                                                .valueMeasured(hours)
                                                                .build();

                                                virtuals.add(ot);
                                                working.add(ot);
                                        }

                                        return new OvertimeProjectionService.ProjectionResult(working, virtuals);
                                });

                var result = auditEngine.audit(ctx, ALLOWED_STATUSES);
                assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Bulk Audit - Should return line, quantity and overtime divergences across days")
        void shouldReturnLineAndOvertimeDivergencesInBulkAudit() {
                var ppu = AuditEngineFixture.ppuEntityAuditEngine();
                var rdoDay1 = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(5), 2);

                var rdoDay2 = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(0), 2);
                rdoDay2.setDate(LocalDate.of(2025, 7, 17));

                var samcDay16 = List.of(
                                AuditEngineFixture.samcRow("1"),
                                AuditEngineFixture.samcRowOvertime("5"),
                                AuditEngineFixture.samcRowFulltime("0.5"),
                                AuditEngineFixture.samcRowEquipment("2"));

                var samcService17 = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("17/07/2025")
                                .numeroDetalhamento("1.1.1.2.1")
                                .descricaoServico("Posto de serviço de Supervisão de movimentação de cargas 12hs")
                                .quantidadeExecutada("2")
                                .status("APROVADO")
                                .build();

                var samcOvertime17 = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("17/07/2025")
                                .numeroDetalhamento("1.1.1.2.5")
                                .descricaoServico(
                                                "Hora extraordinária de posto de serviço de Supervisão de movimentação de cargas")
                                .quantidadeExecutada("3")
                                .status("APROVADO")
                                .build();

                var samcAssistant17 = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("17/07/2025")
                                .numeroDetalhamento("1.1.1.2.8")
                                .descricaoServico("Posto de serviço de Auxiliar de movimentação de cargas 12hs")
                                .quantidadeExecutada("1")
                                .status("APROVADO")
                                .build();

                var samcFulltime17 = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("17/07/2025")
                                .numeroDetalhamento("1.1.1.2.3")
                                .descricaoServico("Posto de serviço de Supervisão de movimentação de cargas 24hs")
                                .quantidadeExecutada("0.5")
                                .status("APROVADO")
                                .build();

                var samcEquipment17 = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("17/07/2025")
                                .descricaoServico("Disponibilidade de máquina lava jato com")
                                .quantidadeExecutada("2")
                                .status("APROVADO")
                                .build();

                var allSamc = new ArrayList<SAMCRow>();
                allSamc.addAll(samcDay16);
                allSamc.addAll(List.of(
                                samcService17,
                                samcOvertime17,
                                samcAssistant17,
                                samcFulltime17,
                                samcEquipment17));

                var ctx = new AuditBatchContext(
                                LocalDate.of(2025, 7, 16),
                                LocalDate.of(2025, 7, 17),
                                ppu,
                                List.of(rdoDay1, rdoDay2),
                                allSamc,
                                Map.of(),
                                Map.of());

                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenAnswer(inv -> {
                                        List<RDOServiceEntity> base = inv.getArgument(0);

                                        double hours = base.stream()
                                                        .filter(Objects::nonNull)
                                                        .map(RDOServiceEntity::getOvertimeHourTotais)
                                                        .filter(Objects::nonNull)
                                                        .mapToDouble(d -> d.toSeconds() / 3600.0)
                                                        .sum();

                                        List<RDOServiceEntity> working = base.stream()
                                                        .map(RDOServiceEntity::copy)
                                                        .collect(Collectors.toCollection(ArrayList::new));

                                        List<RDOServiceEntity> virtuals = new ArrayList<>();

                                        if (hours > 0.0001) {
                                                var ot = RDOServiceEntity.builder()
                                                                .serviceID("overtime-id")
                                                                .serviceNumber("1.1.1.2.5")
                                                                .serviceName("Hora extraordinária de Supervisão")
                                                                .valueMeasured(hours)
                                                                .build();

                                                virtuals.add(ot);
                                                working.add(ot);
                                        }

                                        return new OvertimeProjectionService.ProjectionResult(working, virtuals);
                                });
                var result = auditEngine.audit(ctx, ALLOWED_STATUSES);

                assertEquals(3, result.size());
                assertTrue(result.stream().anyMatch(d -> d.text().contains("quantidade")));
                assertTrue(result.stream().anyMatch(d -> d.line().contains("Hora extraordinária")));
                assertTrue(result.stream().anyMatch(d -> d.text().contains("Linha ausente no RDO")));
        }

        @Test
        @DisplayName("Bulk Audit - Should return header divergences and stop without line checks")
        void shouldReturnHeaderDivergencesInBulkAudit() {
                var ppu = AuditEngineFixture.ppuEntityAuditEngine();

                var rdo1 = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(1), 1);
                var rdo2 = AuditEngineFixture.rdoEntityAuditEngine(Duration.ofHours(1), 1);

                rdo1.setPlatform("PLAT-A");
                rdo2.setPlatform("PLAT-B");

                var samcRow = SAMCRow.builder()
                                .contrato("999999")
                                .local("PLAT-C")
                                .data("01/08/2025")
                                .numeroDetalhamento("1.1.1.2.1")
                                .descricaoServico("Posto de serviço de Supervisão de movimentação de cargas 12hs")
                                .quantidadeExecutada("1")
                                .status("PENDENTE")
                                .build();

                var ctx = new AuditBatchContext(
                                LocalDate.of(2025, 7, 1),
                                LocalDate.of(2025, 7, 31),
                                ppu,
                                List.of(rdo1, rdo2),
                                List.of(samcRow),
                                Map.of(),
                                Map.of());

                var result = auditEngine.audit(ctx, ALLOWED_STATUSES);

                assertEquals(4, result.size());
                assertTrue(result.stream().anyMatch(d -> d.text().contains("Contrato")));
                assertTrue(result.stream().anyMatch(d -> d.text().contains("Plataformas divergentes")));
                assertTrue(result.stream().anyMatch(d -> d.text().contains("Datas do SAMC fora do período")));
                assertTrue(result.stream().anyMatch(d -> d.text().contains("Status do SAMC divergente")));
        }

        @Test
        @DisplayName("Should display real name when available in divergence line")
        void shouldDisplayRealNameInDivergenceLine() {
                LocalDate date = LocalDate.of(2025, 7, 16);

                PPUEntity ppu = PPUEntity.builder()
                                .auditableConfig(AuditableConfig.builder().columnName("descricaoServico").build())
                                .services(List.of(
                                                ServiceLine.builder()
                                                                .id("service-id")
                                                                .genericNumber("1.1.1.2.1")
                                                                .auditableLines(List.of(AuditableLineConfig.builder().label("AUDITCODE001").build()))
                                                                .name("Serviço de Supervisão 12h")
                                                                .type(ServiceType.NORMAL)
                                                                .build()))
                                .build();

                RDOEntity rdo = RDOEntity.builder()
                                .id("rdo-1")
                                .date(date)
                                .platform("Plataforma")
                                .contract(Map.of("codeSap", "400400400"))
                                .services(List.of(
                                                RDOServiceEntity.builder()
                                                                .serviceID("service-id")
                                                                .serviceNumber("1.1.1.2.1")
                                                                .serviceName("Serviço de Supervisão 12h")
                                                                .valueMeasured(2.0)
                                                                .build()))
                                .build();

                SAMCRow samcRow = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("16/07/2025")
                                .numeroDetalhamento("1.1.1.2.1")
                                .descricaoServico("AUDITCODE001")
                                .quantidadeExecutada("1")
                                .status("APROVADO")
                                .build();

                var ctx = new AuditContext(
                                rdo.getDate(),
                                rdo.getPlatform(),
                                ppu,
                                rdo,
                                List.of(samcRow));

                when(overtimeProjectionService.project(anyList(), anyList()))
                                .thenAnswer(inv -> {
                                        List<RDOServiceEntity> base = inv.getArgument(0);
                                        return new OvertimeProjectionService.ProjectionResult(base, List.of());
                                });

                var result = auditEngine.audit(ctx);

                assertEquals(1, result.size());
                var divergence = result.get(0);
                assertTrue(divergence.line().contains("Serviço de Supervisão 12h"));
        }

        @Test
        @DisplayName("Should display only real name when it equals audit name")
        void shouldDisplayOnlyRealNameWhenEqualsAuditName() {
                LocalDate date = LocalDate.of(2025, 7, 16);

                PPUEntity ppu = PPUEntity.builder()
                                .auditableConfig(AuditableConfig.builder().columnName("descricaoServico").build())
                                .services(List.of(
                                                ServiceLine.builder()
                                                                .id("service-id")
                                                                .genericNumber("1.1.1.2.1")
                                                                .auditableLines(List.of(AuditableLineConfig.builder().label("Serviço Normal").build()))
                                                                .name("Serviço Normal")
                                                                .type(ServiceType.NORMAL)
                                                                .build()))
                                .build();

                RDOEntity rdo = RDOEntity.builder()
                                .id("rdo-1")
                                .date(date)
                                .platform("Plataforma")
                                .contract(Map.of("codeSap", "400400400"))
                                .services(List.of(
                                                RDOServiceEntity.builder()
                                                                .serviceID("service-id")
                                                                .serviceNumber("1.1.1.2.1")
                                                                .serviceName("Serviço Normal")
                                                                .valueMeasured(2.0)
                                                                .build()))
                                .build();

                SAMCRow samcRow = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("16/07/2025")
                                .numeroDetalhamento("1.1.1.2.1")
                                .descricaoServico("Serviço Normal")
                                .quantidadeExecutada("1")
                                .status("APROVADO")
                                .build();

                var ctx = new AuditContext(
                                rdo.getDate(),
                                rdo.getPlatform(),
                                ppu,
                                rdo,
                                List.of(samcRow));

                when(overtimeProjectionService.project(anyList(), anyList(), any()))
                                .thenAnswer(inv -> {
                                        List<RDOServiceEntity> base = inv.getArgument(0);
                                        return new OvertimeProjectionService.ProjectionResult(base, List.of());
                                });

                var result = auditEngine.audit(ctx);

                assertEquals(1, result.size());
                var divergence = result.get(0);
                assertTrue(divergence.line().contains("Serviço Normal"));
                assertTrue(!divergence.line().contains("(ref:"));
        }

        @Test
        @DisplayName("Should fallback to audit name when real name is empty")
        void shouldFallbackToAuditNameWhenRealNameIsEmpty() {
                LocalDate date = LocalDate.of(2025, 7, 16);

                PPUEntity ppu = PPUEntity.builder()
                                .auditableConfig(AuditableConfig.builder().columnName("descricaoServico").build())
                                .equipments(List.of(
                                                EquipmentLine.builder()
                                                                .id("equipment-id")
                                                                .genericNumber("0")
                                                                .auditableLines(List.of(AuditableLineConfig.builder().label("Máquina Lava Jato").build()))
                                                                .build()))
                                .build();

                RDOEntity rdo = RDOEntity.builder()
                                .id("rdo-1")
                                .date(date)
                                .platform("Plataforma")
                                .contract(Map.of("codeSap", "400400400"))
                                .equipments(List.of(
                                                RDOEquipment.builder()
                                                                .equipmentPPUId("equipment-id")
                                                                .name("")
                                                                .checkers(List.of(
                                                                                EquipmentChecker.builder()
                                                                                                .operacional(true)
                                                                                                .build()))
                                                                .build()))
                                .build();

                SAMCRow samcRow = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("16/07/2025")
                                .descricaoServico("Máquina Lava Jato")
                                .quantidadeExecutada("2")
                                .status("APROVADO")
                                .build();

                var ctx = new AuditContext(
                                rdo.getDate(),
                                rdo.getPlatform(),
                                ppu,
                                rdo,
                                List.of(samcRow));

                var result = auditEngine.audit(ctx);

                assertEquals(1, result.size());
                var divergence = result.get(0);
                assertTrue(divergence.line().contains("Máquina Lava Jato"));
                assertTrue(!divergence.line().contains("(ref:"));
        }

        @Test
        @DisplayName("Should match SAMC row without EAC number using auditable line")
        void shouldMatchSamcWithoutEacNumberUsingAuditableLine() {
                LocalDate date = LocalDate.of(2025, 7, 16);

                PPUEntity ppu = PPUEntity.builder()
                                .auditableConfig(AuditableConfig.builder().columnName("descricaoServico").build())
                                .services(List.of())
                                .equipments(List.of(
                                                EquipmentLine.builder()
                                                                .genericNumber("0")
                                                                .id("equipment-id")
                                                                .auditableLines(List.of(AuditableLineConfig.builder().label("Disponibilidade de máquina lava jato com").build()))
                                                                .build()))
                                .build();

                RDOEntity rdo = RDOEntity.builder()
                                .id("rdo-1")
                                .date(date)
                                .platform("Plataforma")
                                .contract(Map.of("codeSap", "400400400"))
                                .equipments(List.of(
                                                RDOEquipment.builder()
                                                                .equipmentPPUId("equipment-id")
                                                                .name("Disponibilidade de máquina lava jato com")
                                                                .checkers(List.of(
                                                                                EquipmentChecker.builder()
                                                                                                .operacional(true)
                                                                                                .build(),
                                                                                EquipmentChecker.builder()
                                                                                                .operacional(true)
                                                                                                .build()))
                                                                .build()))
                                .build();

                SAMCRow samcRow = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .data("16/07/2025")
                                .numeroDetalhamento(null)
                                .descricaoServico("Disponibilidade de máquina lava jato com")
                                .quantidadeExecutada("2")
                                .status("APROVADO")
                                .build();

                var ctx = new AuditContext(
                                rdo.getDate(),
                                rdo.getPlatform(),
                                ppu,
                                rdo,
                                List.of(samcRow));

                var result = auditEngine.audit(ctx);

                assertTrue(result.isEmpty(), "Expected no divergences when matching by auditable line fallback");
        }

        @Test
        @DisplayName("Bulk Audit - Should compare SAMC multi-day rows against summed RDO interval")
        void shouldCompareSamcMultiDayRowsAgainstSummedRdoInterval() {
                var ppu = AuditEngineFixture.ppuEntityAuditEngine();
                var day16 = RDOEntity.builder()
                                .id("rdo-16")
                                .date(LocalDate.of(2025, 7, 16))
                                .contract(Map.of("codeSap", "400400400"))
                                .platform("Plataforma")
                                .services(List.of(AuditEngineFixture.serviceEntity(Duration.ZERO)))
                                .build();
                var day17 = RDOEntity.builder()
                                .id("rdo-17")
                                .date(LocalDate.of(2025, 7, 17))
                                .contract(Map.of("codeSap", "400400400"))
                                .platform("Plataforma")
                                .services(List.of(AuditEngineFixture.serviceEntity(Duration.ZERO)))
                                .build();

                String normalService = "Posto de servi\u00E7o de Supervis\u00E3o de movimenta\u00E7\u00E3o de cargas 12hs";
                var samcRange = SAMCRow.builder()
                                .contrato("400400400")
                                .local("Plataforma")
                                .dataInicio("16/07/2025")
                                .data("17/07/2025")
                                .numeroDetalhamento("1.1.1.2.1")
                                .descricaoServico(normalService)
                                .auditableValue(normalService)
                                .quantidadeExecutada("2")
                                .status("APROVADO")
                                .build();

                var ctx = new AuditBatchContext(
                                LocalDate.of(2025, 7, 16),
                                LocalDate.of(2025, 7, 17),
                                ppu,
                                List.of(day16, day17),
                                List.of(samcRange),
                                Map.of(),
                                Map.of());

                var result = auditEngine.audit(ctx, ALLOWED_STATUSES);

                assertTrue(result.isEmpty(), result.toString());
        }

        public static class AuditEngineFixture {
                public static AuditRDORow auditRDORowOvertime(String quantidade) {
                        return AuditRDORow.builder()
                                        .number("1.1.1.2.5")
                                        .auditName("Hora extraordinária de posto de serviço de Supervisão de movimentação de cargas")
                                        .quantity(quantidade)
                                        .fullTime(false)
                                        .build();
                }

                public static AuditRDORow auditRDORow(String quantidade) {
                        return AuditRDORow.builder()
                                        .number("1.1.1.2.1")
                                        .auditName("Posto de serviço de Supervisão de movimentação de cargas 12hs")
                                        .quantity(quantidade)
                                        .fullTime(false)
                                        .build();
                }

                public static AuditRDORow auditRDORowFulltime(String quantidade) {
                        return AuditRDORow.builder()
                                        .number("1.1.1.2.3")
                                        .auditName("Posto de serviço de Supervisão de movimentação de cargas 24hs")
                                        .quantity(quantidade)
                                        .fullTime(true)
                                        .build();
                }

                public static AuditRDORow equipmentRow(String quantidade) {
                        return AuditRDORow.builder()
                                        .number("0")
                                        .auditName("Disponibilidade de máquina lava jato com")
                                        .quantity(quantidade)
                                        .fullTime(false)
                                        .build();
                }

                public static SAMCRow samcRowOvertime(String quantidade) {
                        return SAMCRow.builder()
                                        .contrato("400400400")
                                        .local("Plataforma")
                                        .data("16/07/2025")
                                        .numeroDetalhamento("1.1.1.2.5")
                                        .descricaoServico(
                                                        "Hora extraordinária de posto de serviço de Supervisão de movimentação de cargas")
                                        .auditableValue("Hora extraordinária de posto de serviço de Supervisão de movimentação de cargas")
                                        .quantidadeExecutada(quantidade)
                                        .status("APROVADO")
                                        .build();
                }

                public static SAMCRow samcRow(String quantidade) {
                        return SAMCRow.builder()
                                        .contrato("400400400")
                                        .local("Plataforma")
                                        .data("16/07/2025")
                                        .numeroDetalhamento("1.1.1.2.1")
                                        .descricaoServico(
                                                        "Posto de serviço de Supervisão de movimentação de cargas 12hs")
                                        .auditableValue("Posto de serviço de Supervisão de movimentação de cargas 12hs")
                                        .quantidadeExecutada(quantidade)
                                        .status("APROVADO")
                                        .build();
                }

                public static SAMCRow samcRowAssistant(String quantidade) {
                        return SAMCRow.builder()
                                        .contrato("400400400")
                                        .local("Plataforma")
                                        .data("16/07/2025")
                                        .numeroDetalhamento("1.1.1.2.8")
                                        .descricaoServico("Posto de serviço de Auxiliar de movimentação de cargas 12hs")
                                        .auditableValue("Posto de serviço de Auxiliar de movimentação de cargas 12hs")
                                        .quantidadeExecutada(quantidade)
                                        .status("APROVADO")
                                        .build();
                }

                public static SAMCRow samcRowFulltime(String quantidade) {
                        return SAMCRow.builder()
                                        .contrato("400400400")
                                        .local("Plataforma")
                                        .data("16/07/2025")
                                        .numeroDetalhamento("1.1.1.2.3")
                                        .descricaoServico(
                                                        "Posto de serviço de Supervisão de movimentação de cargas 24hs")
                                        .auditableValue("Posto de serviço de Supervisão de movimentação de cargas 24hs")
                                        .quantidadeExecutada(quantidade)
                                        .status("APROVADO")
                                        .build();
                }

                public static SAMCRow samcRowEquipment(String quantidade) {
                        return SAMCRow.builder()
                                        .contrato("400400400")
                                        .local("Plataforma")
                                        .data("16/07/2025")
                                        .descricaoServico("Disponibilidade de máquina lava jato com")
                                        .auditableValue("Disponibilidade de máquina lava jato com")
                                        .quantidadeExecutada(quantidade)
                                        .status("APROVADO")
                                        .build();
                }

                private static ServiceLine serviceLineOvertime() {
                        return ServiceLine.builder()
                                        .disposicao(false)
                                        .id("overtime-id")
                                        .type(ServiceType.HORAEXTRA)
                                        .name("Hora extraordinária de Supervisão")
                                        .genericNumber("1.1.1.2.5")
                                        .auditableLines(List.of(AuditableLineConfig.builder().label("Hora extraordinária de posto de serviço de Supervisão de movimentação de cargas").build()))
                                        .isOvertimeService(true)
                                        .build();
                }

                private static ServiceLine serviceLine() {
                        return ServiceLine.builder()
                                        .disposicao(false)
                                        .id("normal-id")
                                        .type(ServiceType.NORMAL)
                                        .genericNumber("1.1.1.2.1")
                                        .children(List.of("1.1.1.2.5"))
                                        .overtimeService("overtime-id")
                                        .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                                        .auditableLines(List.of(AuditableLineConfig.builder().label("Posto de serviço de Supervisão de movimentação de cargas 12hs").build()))
                                        .isOvertimeService(false)
                                        .build();
                }

                private static ServiceLine serviceLineFulltime() {
                        return ServiceLine.builder()
                                        .disposicao(false)
                                        .id("fulltime-id")
                                        .type(ServiceType.VINTEQUATROHORAS)
                                        .genericNumber("1.1.1.2.3")
                                        .overtimeService("overtime-id")
                                        .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                                        .auditableLines(List.of(AuditableLineConfig.builder().label("Posto de serviço de Supervisão de movimentação de cargas 24hs").build()))
                                        .build();
                }

                private static EquipmentLine equipmentLine() {
                        return EquipmentLine.builder()
                                        .genericNumber("0")
                                        .id("equipment-id")
                                        .auditableLines(List.of(AuditableLineConfig.builder().label("Disponibilidade de máquina lava jato com").build()))
                                        .build();
                }

                public static PPUEntity ppuEntityAuditEngine() {
                        return PPUEntity.builder()
                                        .auditableConfig(AuditableConfig.builder().columnName("descricaoServico")
                                                        .build())
                                        .services(List.of(serviceLine(), serviceLineOvertime(), serviceLineFulltime()))
                                        .equipments(List.of(equipmentLine()))
                                        .build();
                }

                static RDOServiceEntity serviceEntity(Duration quantity) {
                        return RDOServiceEntity.builder()
                                        .serviceNumber("1.1.1.2.1")
                                        .serviceID("normal-id")
                                        .overtimeHourTotais(quantity)
                                        .valueMeasured(1.0)
                                        .build();
                }

                static RDOServiceEntity serviceEntityFulltime() {
                        return RDOServiceEntity.builder()
                                        .serviceNumber("1.1.1.2.3")
                                        .serviceID("fulltime-id")
                                        .valueMeasured(0.5)
                                        .build();
                }

                private static RDOEquipment equipmentRDOAuditEngine(Integer quantity) {
                        var items = new ArrayList<EquipmentChecker>();
                        for (var i = 0; i < quantity; i++) {
                                items.add(equipmentChecker());
                        }

                        return RDOEquipment.builder()
                                        .equipmentPPUId(equipmentLine().getId())
                                        .checkers(items)
                                        .build();
                }

                private static EquipmentChecker equipmentChecker() {
                        return EquipmentChecker.builder()
                                        .operacional(true)
                                        .build();
                }

                public static RDOEntity rdoEntityAuditEngine(Duration quantityHours, Integer quantityEquipments) {
                        return RDOEntity.builder()
                                        .date(LocalDate.of(2025, 7, 16)).contract(Map.of("codeSap", "400400400"))
                                        .platform("Plataforma")
                                        .services(List.of(serviceEntity(quantityHours), serviceEntityFulltime()))
                                        .equipments(List.of(equipmentRDOAuditEngine(quantityEquipments)))
                                        .build();
                }

                public static Answer<OvertimeProjectionService.ProjectionResult> byOvertimeTotals(
                                String overtimeServiceId,
                                String overtimeServiceNumber,
                                String overtimeServiceName) {
                        return inv -> {
                                List<RDOServiceEntity> base = inv.getArgument(0);

                                double hours = base.stream()
                                                .filter(Objects::nonNull)
                                                .map(RDOServiceEntity::getOvertimeHourTotais)
                                                .filter(Objects::nonNull)
                                                .mapToDouble(d -> d.toSeconds() / 3600.0)
                                                .sum();

                                List<RDOServiceEntity> working = base.stream()
                                                .map(RDOServiceEntity::copy)
                                                .collect(Collectors.toCollection(ArrayList::new));

                                List<RDOServiceEntity> virtuals = new ArrayList<>();

                                if (hours > 0.0001) {
                                        var ot = RDOServiceEntity.builder()
                                                        .serviceID(overtimeServiceId)
                                                        .serviceNumber(overtimeServiceNumber)
                                                        .serviceName(overtimeServiceName)
                                                        .valueMeasured(hours)
                                                        .build();

                                        virtuals.add(ot);
                                        working.add(ot);
                                }

                                return new OvertimeProjectionService.ProjectionResult(working, virtuals);
                        };
                }
        }

}
