package com.indux.modules.ppu.application.services.fixtures;

import com.indux.modules.ppu.application.dtos.rdo.SAMCData;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditDivergence;
import com.indux.modules.ppu.application.services.rdo.operation.audit.AuditEquivalence;
import com.indux.modules.ppu.domain.entities.rdo.audit.SAMCRow;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.domain.entities.rdo.SteelCableChecker;
import com.indux.modules.ppu.domain.entities.rdo.EquipmentChecker;
import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuditSAMCFixture {

        public static final String RDO_ID = "688cf96e1a52225f8562b6ac";
        public static final String PPU_ID = "6849cf01c7b0250cfab80edc";
        public static final String PLATFORM = "P-58";
        public static final Long SEQUENTIAL_ID = 1L;
        public static final LocalDate DATE = LocalDate.of(2025, 7, 25);
        public static final String CREATOR_REGISTRATION = "8581";
        public static final String CREATOR_NAME = "ALAN IMBELONI DE OLIVEIRA";
        public static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 8, 1, 17, 29, 18);
        public static final String CREATED_BY = "WEB";
        public static final String CREATOR_POSITION = "Supervisor de Teste";
        public static final String CLIENT_NAME = "PETROBRAS";
        public static final String REGIONAL_NAME = "RJ";
        public static final String COMPETENCE = "-";

        public static final Map<String, Object> CONTRACT = Map.of(
                        "_id", 313,
                        "rateio", 99,
                        "costCenterName", "OS 164/2023 - LOTE IV",
                        "megaId", 93,
                        "projectName", "LOTE 04 UN-ES - MOV. CARGAS",
                        "codeSap", "4600677524",
                        "idCliente", "7",
                        "id", 313);

        public static final ClientEmployee CLIENT_EMPLOYEE = new ClientEmployee(
                        "",
                        "",
                        "");

        public static final List<RDOEquipment> EQUIPMENTS = List.of(
                        RDOEquipment.builder()
                                        .id("dd3d163d-cf58-466a-a0f2-dc7f9158a3f9")
                                        .number("2.4")
                                        .name("Disponibilidade de Máquina de Lava Jato com Acessórios - HD1016")
                                        .checkers(List.of(
                                                        new EquipmentChecker(
                                                                        "dd3d163d-cf58-466a-a0f2-dc7f9158a3f9",
                                                                        "HL-127",
                                                                        "KARCHER",
                                                                        "HD1016",
                                                                        true,
                                                                        ""),
                                                        new EquipmentChecker(
                                                                        "0c871362-d740-4809-93f5-7749ca0bf87e",
                                                                        "HL-045",
                                                                        "KARCHER",
                                                                        "HDS12/15",
                                                                        true,
                                                                        ""),
                                                        new EquipmentChecker(
                                                                        "6b65bda3-bb35-420c-8489-0f10c4c8a6b8",
                                                                        "HL088",
                                                                        "GHIBLI",
                                                                        "17/185",
                                                                        false,
                                                                        "")

                                        ))
                                        .build(),
                        RDOEquipment.builder()
                                        .id("74073b93-3760-4920-b4a1-49ddc0ddad28")
                                        .number("2.4")
                                        .name("Disponibilidade de Máquina de Lava Jato com Acessórios - HD1016")
                                        .checkers(List.of(
                                                        new EquipmentChecker(
                                                                        "74073b93-3760-4920-b4a1-49ddc0ddad28",
                                                                        "HL-128",
                                                                        "KARCHER",
                                                                        "HD1016",
                                                                        true,
                                                                        "")))
                                        .build(),
                        RDOEquipment.builder()
                                        .id("445e0d4e-735a-4d1a-9f08-249487f56256")
                                        .number("2.5")
                                        .name("Disponibilidade de Máquina Desentupidora com Acessórios - ")
                                        .checkers(List.of(
                                                        new EquipmentChecker(
                                                                        "445e0d4e-735a-4d1a-9f08-249487f56256",
                                                                        "12792",
                                                                        "RIDGID",
                                                                        "",
                                                                        true,
                                                                        "")))
                                        .build());

        public static final List<SteelCableChecker> STEEL_CABLE = List.of(
                        new SteelCableChecker(
                                        "3c8d573d-1b06-4744-9bb4-bc61f657a6ac",
                                        "3.10",
                                        "Cabos de Aço AUXILIAR da LANÇA do Guindaste MEP P-58",
                                        null,
                                        "Dia",
                                        250.0,
                                        1.0,
                                        2.0,
                                        null, null),
                        new SteelCableChecker(
                                        "22c8a90e-cd72-418d-958c-356493f67ed4",
                                        "3.11",
                                        "Cabos de Aço PRINCIPAL da LANÇA do Guindaste MEP P-58",
                                        null,
                                        "Dia",
                                        250.0,
                                        1.0,
                                        0.0,
                                        null, null),
                        new SteelCableChecker(
                                        "6e2d987f-ac50-4ea9-acd5-9f150149660c",
                                        "3.12",
                                        "Cabos de Aço da LANÇA do Guindaste MEP P-58",
                                        null,
                                        "Dia",
                                        250.0,
                                        1.0,
                                        2.0,
                                        null, null));

        public static final List<AccessoryKitDTO> ACCESSORY_KITS = List.of(
                        new AccessoryKitDTO(
                                        "9b64dd7e-cdc2-488c-b4ed-9ac945c67072",
                                        "1.0.0",
                                        "1.0.0",
                                        "Disponibilidade de Conjunto de Eslingas, Acessórios e Equipos de Mov. Cargas",
                                        "Conjunto completo",
                                        "UN",
                                        0.0,
                                        1.0,
                                        true,
                                        true,
                                        "",
                                        null,
                                        null, 1.0, null));

        public static final List<CraneControl> CRANE_CONTROL = List.of(
                        new CraneControl(
                                        "cb9aba38-bba4-4872-bc69-610369c31f38",
                                        "SOUTH_FACE",
                                        LocalDateTime.of(2024, 4, 16, 13, 0),
                                        true,
                                        "1980",
                                        false,
                                        new ArrayList<>(),
                                        "",
                                        "",
                                        "PRA-1",
                                        List.of()),
                        new CraneControl(
                                        "e503c2e3-97ed-451c-869d-d0700383de07",
                                        "NORTH_FACE",
                                        LocalDateTime.of(2024, 4, 16, 13, 0),
                                        true,
                                        "1980",
                                        false,
                                        new ArrayList<>(),
                                        "",
                                        "",
                                        "PRA-1",
                                        List.of()));

        public static final List<SteelCableControl> CABLE_CONTROL = List.of(
                        new SteelCableControl(
                                        "NORTH_FACE",
                                        "Principal",
                                        true,
                                        null,
                                        false,
                                        null,
                                        "PRA-1"),
                        new SteelCableControl(
                                        "NORTH_FACE",
                                        "Auxiliar",
                                        true,
                                        null,
                                        false,
                                        null,
                                        "PRA-1"),
                        new SteelCableControl(
                                        "NORTH_FACE",
                                        "Lança",
                                        true,
                                        null,
                                        false,
                                        null,
                                        "PRA-1"),
                        new SteelCableControl(
                                        "SOUTH_FACE",
                                        "Principal",
                                        true,
                                        null,
                                        false,
                                        null,
                                        "PRA-1"),
                        new SteelCableControl(
                                        "SOUTH_FACE",
                                        "Auxiliar",
                                        true,
                                        null,
                                        false,
                                        null,
                                        "PRA-1"),
                        new SteelCableControl(
                                        "SOUTH_FACE",
                                        "Lança",
                                        true,
                                        null,
                                        false,
                                        null,
                                        "PRA-1"));

        public static final List<RDOServiceEntity> SERVICES = List.of(
                        RDOServiceEntity.builder()
                                        .id("08741735-0ae0-4b2a-a061-5f14b776829f")
                                        .serviceID("495ba9cd-5168-4236-b8dd-6c5f8b7527c0")
                                        .serviceName("Serviço de Supervisão de Movimentação de Cargas")
                                        .serviceNumber("1.1.101")
                                        .registration("11974")
                                        .name("EDILENO PESTANA DA SILVA")
                                        .cargoID("99.4.01")
                                        .cargoNome("SUPERVISOR DE MOV CARGAS")
                                        .present(true)
                                        .schedule(ShiftSchedule.builder()
                                                        .turno("Diurno")
                                                        .horarioInicial(LocalTime.of(10, 0))
                                                        .horarioFinal(LocalTime.of(22, 0))
                                                        .build())
                                        .dayType("rotina")
                                        .horaChegadaVoo(LocalTime.of(10, 0))
                                        .flagman(false)
                                        .overtimes(List.of())
                                        .sispat("")
                                        .teamLeader(false)
                                        .nightShiftPremium(Duration.ZERO)
                                        .normalHours(Duration.ZERO)
                                        .hourTotais(Duration.ofHours(11))
                                        .overtimeHourTotais(Duration.ZERO)
                                        .statusEmployee("Embarque")
                                        .build(),
                        RDOServiceEntity.builder()
                                        .id("2ac5f10e-4378-43fd-9c48-a91eaee9b18c")
                                        .serviceID("e15550c8-16dc-4aa7-beda-95a4ce0fd348")
                                        .serviceName("Serviço de Sinaleiro")
                                        .serviceNumber("1.1.116")
                                        .registration("12029")
                                        .name("JAMILTON SOUSA DOS SANTOS ")
                                        .cargoID("99.4.03")
                                        .cargoNome("AUX DE MOV DE CARGAS SENIOR")
                                        .present(true)
                                        .schedule(ShiftSchedule.builder()
                                                        .turno("Diurno")
                                                        .horarioInicial(LocalTime.of(10, 0))
                                                        .horarioFinal(LocalTime.of(22, 0))
                                                        .build())
                                        .dayType("rotina")
                                        .horaChegadaVoo(LocalTime.of(10, 0))
                                        .flagman(true)
                                        .overtimes(List.of())
                                        .sispat("")
                                        .teamLeader(true)
                                        .nightShiftPremium(Duration.ZERO)
                                        .normalHours(Duration.ZERO)
                                        .hourTotais(Duration.ofHours(11))
                                        .overtimeHourTotais(Duration.ZERO)
                                        .statusEmployee("Embarque")
                                        .build());

        public static final RDOServiceEntity availableService = RDOServiceEntity.builder()
                        .id("2acf3432e-4378-43fd-9c48-a91eaee9b18c")
                        .serviceID("e15550c8-1442-4aa7-beda-95a4ce0fd348")
                        .serviceName("Serviço de Sinaleiro à disposição")
                        .serviceNumber("1.1.117")
                        .registration("12028")
                        .name("JANUEIRO DE TESTE")
                        .cargoID("99.4.03")
                        .cargoNome("AUX DE MOV DE CARGAS SENIOR")
                        .present(true)
                        .schedule(ShiftSchedule.builder()
                                        .turno("Diurno")
                                        .horarioInicial(LocalTime.of(0, 0))
                                        .horarioFinal(LocalTime.of(0, 0))
                                        .build())
                        .dayType("rotina")
                        .flagman(true)
                        .overtimes(List.of())
                        .sispat("")
                        .teamLeader(true)
                        .nightShiftPremium(Duration.ZERO)
                        .normalHours(Duration.ZERO)
                        .hourTotais(Duration.ofHours(11))
                        .overtimeHourTotais(Duration.ZERO)
                        .statusEmployee("Embarque")
                        .build();

        public static final String CONTRACTOR_SERVICES = "221";

        public static RDOEntity createAuditSAMCEntity() {
                return RDOEntity.builder()
                                .id(RDO_ID)
                                .ppuId(PPU_ID)
                                .platform(PLATFORM)
                                .sequentialId(SEQUENTIAL_ID)
                                .date(DATE)
                                .creatorRegistration(CREATOR_REGISTRATION)
                                .creatorName(CREATOR_NAME)
                                .createdAt(CREATED_AT)
                                .createdBy(CREATED_BY)
                                .creatorPosition(CREATOR_POSITION)
                                .contract(CONTRACT)
                                .clientName(CLIENT_NAME)
                                .regionalNome(REGIONAL_NAME)
                                .competence(COMPETENCE)
                                .statusDP(RDOStatusDP.PENDING)
                                .statusOP(RDOStatusOP.PENDING)
                                .clientEmployee(CLIENT_EMPLOYEE)
                                .contractorObservations("")
                                .equipments(EQUIPMENTS)
                                .totalPlannedEquipments(null)
                                .steelCable(STEEL_CABLE)
                                .accessoryKits(ACCESSORY_KITS)
                                .craneControl(CRANE_CONTROL)
                                .cableControl(CABLE_CONTROL)
                                .employeeDepartures(new ArrayList<>())
                                .services(SERVICES)
                                .contractorServices(CONTRACTOR_SERVICES)
                                .attachments(new ArrayList<>())
                                .loggers(new ArrayList<>())
                                .divergences(new ArrayList<>())
                                .build();
        }

        // Dados do EXCEL
        public static final List<SAMCData> SAMC_EXCEL_DATA = List.of(
                        new SAMCData("1.1.110", "7", "Serviço de Auxiliar de Movimentação de Cargas"),
                        new SAMCData("1.1.112", "10", "Serviço Extraordinário de Auxiliar de Movimentação de Cargas"),
                        new SAMCData("1.1.116", "1", "Serviço de Sinaleiro"),
                        new SAMCData("1.1.101", "1", "Serviço de Supervisão de Movimentação de Cargas"),
                        new SAMCData("1.1.104", "2", "Serviço de Operação de Guindaste"),
                        new SAMCData("3.10", "2", "Cabos de Aço AUXILIAR da LANÇA do Guindaste MEP P-58"),
                        new SAMCData("3.12", "2", "Cabos de Aço da LANÇA do Guindaste MEP P-58"));

        public static final List<AuditEquivalence> AUDIT_EQUIVALENCES = List.of(
                        new AuditEquivalence(
                                        RDO_ID,
                                        "1.1.101",
                                        "1.1.101",
                                        "Serviço de Supervisão de Movimentação de Cargas",
                                        "Serviço de Supervisão de Movimentação de Cargas",
                                        "1",
                                        "1"),
                        new AuditEquivalence(
                                        RDO_ID,
                                        "1.1.110",
                                        "1.1.110",
                                        "Serviço de Auxiliar de Movimentação de Cargas",
                                        "Serviço de Auxiliar de Movimentação de Cargas",
                                        "7",
                                        "7"),
                        new AuditEquivalence(
                                        RDO_ID,
                                        "1.1.104",
                                        "1.1.104",
                                        "Serviço de Operação de Guindaste",
                                        "Serviço de Operação de Guindaste",
                                        "1",
                                        "1"),
                        new AuditEquivalence(
                                        RDO_ID,
                                        "1.1.116",
                                        "1.1.116",
                                        "Serviço de Sinaleiro",
                                        "Serviço de Sinaleiro",
                                        "1",
                                        "1"),
                        new AuditEquivalence(
                                        RDO_ID,
                                        "3.10",
                                        "3.10",
                                        "Cabos de Aço AUXILIAR da LANÇA do Guindaste MEP P-58",
                                        "Cabos de Aço AUXILIAR da LANÇA do Guindaste MEP P-58",
                                        "2",
                                        "2"));

        public static final List<AuditDivergence> AUDIT_DIVERGENCES = List.of(
                        new AuditDivergence(
                                        RDO_ID,
                                        "1.1.999",
                                        "",
                                        "",
                                        DATE.toString(),
                                        "Linha não encontrada no RDO",
                                        "1.1.999",
                                        ""),
                        new AuditDivergence(
                                        RDO_ID,
                                        "-",
                                        "4600677524",
                                        "4600677525",
                                        DATE.toString(),
                                        "Código SAP do contrato não confere.", "null", null),
                        new AuditDivergence(
                                        RDO_ID,
                                        "-",
                                        PLATFORM,
                                        "P-59",
                                        DATE.toString(),
                                        "Plataforma não confere.", null, ""),
                        new AuditDivergence(
                                        RDO_ID,
                                        "-",
                                        "25/07/2025",
                                        "26/07/2025",
                                        DATE.toString(),
                                        "Data não confere.", null, ""));

        public static final String EXCEL_COL1_SAP_CODE = "4600677524";
        public static final String EXCEL_COL3_PLATFORM = "P-58";
        public static final String EXCEL_COL5_DATE = "25/07/2025";

        public static RDOServiceEntity createServiceWithOvertime(String serviceNumber, String serviceName,
                        LocalTime overtimeStart, LocalTime overtimeEnd) {
                return RDOServiceEntity.builder()
                                .id(UUID.randomUUID().toString())
                                .serviceID(UUID.randomUUID().toString())
                                .serviceName(serviceName)
                                .serviceNumber(serviceNumber)
                                .registration("12345")
                                .name("JOÃO DA SILVA")
                                .cargoID("99.4.01")
                                .cargoNome("AUXILIAR DE MOVIMENTAÇÃO")
                                .present(true)
                                .schedule(ShiftSchedule.builder()
                                                .turno("Diurno")
                                                .horarioInicial(LocalTime.of(7, 0))
                                                .horarioFinal(LocalTime.of(19, 0))
                                                .build())
                                .dayType("rotina")
                                .flagman(false)
                                .overtimes(List.of(new EmployeeOvertime(overtimeStart, overtimeEnd)))
                                .sispat("")
                                .teamLeader(false)
                                .nightShiftPremium(Duration.ZERO)
                                .normalHours(Duration.ofHours(8))
                                .hourTotais(Duration.ofHours(10))
                                .overtimeHourTotais(Duration.ofHours(2))
                                .statusEmployee("Embarque")
                                .build();
        }

        public static PPUEntity createPPUWithOvertimeServices() {

                ServiceLine parentService = new ServiceLine();
                parentService.setGenericNumber("1.1.110");
                parentService.setName("Serviço de Auxiliar de Movimentação de Cargas");
                parentService.setIsOvertimeService(false);
                parentService.setChildren(List.of("1.1.112"));

                ServiceLine overtimeService = new ServiceLine();
                overtimeService.setGenericNumber("1.1.112");
                overtimeService.setName("Serviço Extraordinário de Auxiliar de Movimentação de Cargas");
                overtimeService.setIsOvertimeService(true);

                PPUEntity ppuEntity = new PPUEntity();
                ppuEntity.setId(PPU_ID);
                ppuEntity.setServices(List.of(parentService, overtimeService));
                return ppuEntity;
        }

        public static RDOEntity createRDOWithOvertimeServices() {
                RDOServiceEntity serviceWithOvertime = createServiceWithOvertime(
                                "1.1.110",
                                "Serviço de Auxiliar de Movimentação de Cargas",
                                LocalTime.of(17, 0),
                                LocalTime.of(19, 0));

                return RDOEntity.builder()
                                .id(RDO_ID)
                                .ppuId(PPU_ID)
                                .platform(PLATFORM)
                                .date(DATE)
                                .contract(CONTRACT)
                                .services(List.of(serviceWithOvertime))
                                .equipments(new ArrayList<>())
                                .steelCable(new ArrayList<>())
                                .accessoryKits(new ArrayList<>())
                                .employeeDepartures(new ArrayList<>())
                                .attachments(new ArrayList<>())
                                .loggers(new ArrayList<>())
                                .divergences(new ArrayList<>())
                                .build();
        }

        public static RDOEntity createRDOWithoutOvertimeServices() {
                RDOServiceEntity serviceWithOvertime = createServiceWithOvertime(
                                "1.1.110",
                                "Serviço de Auxiliar de Movimentação de Cargas",
                                null,
                                null);

                return RDOEntity.builder()
                                .id(RDO_ID)
                                .ppuId(PPU_ID)
                                .platform(PLATFORM)
                                .date(DATE)
                                .contract(CONTRACT)
                                .services(List.of(serviceWithOvertime))
                                .equipments(new ArrayList<>())
                                .steelCable(new ArrayList<>())
                                .accessoryKits(new ArrayList<>())
                                .employeeDepartures(new ArrayList<>())
                                .attachments(new ArrayList<>())
                                .loggers(new ArrayList<>())
                                .divergences(new ArrayList<>())
                                .build();
        }

        public static SAMCRow createOvertimeServiceSamcRow(String hours) {
                return SAMCRow.builder()
                                .contrato(EXCEL_COL1_SAP_CODE)
                                .local(EXCEL_COL3_PLATFORM)
                                .data(EXCEL_COL5_DATE)
                                .numeroDetalhamento("1.1.112")
                                .descricaoServico("Serviço Extraordinário de Auxiliar de Movimentação de Cargas")
                                .quantidadeExecutada(hours)
                                .status("Aprovado")
                                .auditableValue("Serviço Extraordinário de Auxiliar de Movimentação de Cargas")
                                .build();
        }
}
