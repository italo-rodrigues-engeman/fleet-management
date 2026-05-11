package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.indux.modules.ppu.application.services.rdo.operation.bm.BMFixtures.createFakePPUToBMTimeline;
import static com.indux.modules.ppu.application.services.rdo.operation.bm.BMFixtures.createMultiPlatformPPU;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class BMMonthlyHandlerTest {
    String supervisorID = "495ba9cd-5168-4236-b8dd-6c5f8b7527c0";
    String assistantID = "2df2181f-e99b-440c-9b5a-45a9423e3365";
    String assistantOvertimeId = "52676105-5c28-47d9-a562-2f44b01ba967";


    @Mock
    private PPURepository ppuRepository;
    @Mock
    private RDORepository rdoRepository;
    @InjectMocks
    private BMMonthlyHandler handler;
    @Mock
    private BMRepository repository;
    @Spy
    private BMMapper mapper = Mappers.getMapper(BMMapper.class);

    @Mock private OvertimeProjectionService overtimeProjectionService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should fetch Monthly BM calculation correctly")
    void fetchMonthlyBM() {
        var ppu = createFakePPUToBMTimeline();


        var start = LocalDate.of(2025, 10, 1);
        var end = LocalDate.of(2025, 10, 3);
        var project = 44L;

        var range = new DateRange(start, end);
        var rdo1 = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3))
        ));
        rdo1.setPlatform("P-TESTE");
        rdo1.setDate(start);

        var rdo2 = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(2))
        ));
        rdo2.setPlatform("P-PROD");
        rdo2.setDate(start);

        BMEntity bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();
        var rdos = List.of(rdo1, rdo2);
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(rdos);
        when(overtimeProjectionService.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionService.ProjectionResult(List.of(
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3)),
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(2))
                        ), List.of(
                        RDOServiceEntity.builder().serviceID(assistantOvertimeId).valueMeasured(5.0).build()
                )));

        var response = handler.fetchMonthlyBM("BMID");

        assertEquals(6, response.size());

        response.forEach(item -> {
            assertNotNull(item.getId());
            assertNotNull(item.getNumber());
            assertNotNull(item.getName());
            assertNotNull(item.getQuantity());
            assertNotNull(item.getTotal());
        });

        var supervisorItem = response.stream()
                .filter(t -> supervisorID.equals(t.getId()))
                .findFirst()
                .orElseThrow();

        assertNotNull(supervisorItem);
        assertTrue((Double) supervisorItem.getQuantity() >= 0.0);
        assertTrue(supervisorItem.getTotal().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Should calculate Monthly BM for multiple platforms correctly")
    void fetchMonthlyBM_multiPlatform() {
        var ppu = createMultiPlatformPPU();
        var start = LocalDate.of(2025, 10, 1);
        var end = LocalDate.of(2025, 10, 2);
        var project = 44L;

        var rdo1 = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(4))
        ));
        rdo1.setPlatform("P-TESTE");
        rdo1.setDate(start);

        var range = new DateRange(start, end);
        var rdo2 = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3))
        ));
        rdo2.setPlatform("P-PROD");
        rdo2.setDate(start);
        BMEntity bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();
        var rdos = List.of(rdo1, rdo2);

        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE", "P-PROD"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(rdos);

        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(overtimeProjectionService.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionService.ProjectionResult(List.of(
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(4)),
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3))
                ), List.of(
                        RDOServiceEntity.builder().serviceID(assistantOvertimeId).valueMeasured(7.0).build()
                )));


        var response = handler.fetchMonthlyBM("BMID");

        assertEquals(6, response.size());

        response.forEach(item -> {
            assertNotNull(item.getId());
            assertNotNull(item.getNumber());
            assertNotNull(item.getName());
            assertNotNull(item.getQuantity());
            assertNotNull(item.getTotal());
        });

        var supervisorItem = response.stream()
                .filter(t -> supervisorID.equals(t.getId()))
                .findFirst()
                .orElseThrow();

        assertTrue((Double) supervisorItem.getQuantity() >= 0.0);
        assertTrue(supervisorItem.getTotal().compareTo(java.math.BigDecimal.ZERO) >= 0.0);
    }

    @Test
    @DisplayName("Should return empty quantities when no RDOs are present")
    void fetchMonthlyBM_noRDOs() {
        var ppu = createFakePPUToBMTimeline();
        var start = LocalDate.of(2025, 10, 1);
        var end = LocalDate.of(2025, 10, 1);
        var project = 44L;

        var range = new DateRange(start, end);
        BMEntity bm = BMEntity.builder()
                .id("BMID")
                .projectId(project)
                .ppu(ppu)
                .period(range)
                .build();
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(List.of());
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));

        when(overtimeProjectionService.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionService.ProjectionResult(List.of(), List.of()));

        var response = handler.fetchMonthlyBM("BMID");

        assertEquals(6, response.size());

        response.forEach(item -> {
            assertNotNull(item.getId());
            assertNotNull(item.getNumber());
            assertNotNull(item.getName());
            assertEquals(0.0, item.getQuantity());
            assertEquals(0.0, item.getTotal().compareTo(BigDecimal.ZERO));
        });
    }
}