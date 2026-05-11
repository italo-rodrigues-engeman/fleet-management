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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

class BMPlatformHandlerTest {

    String supervisorID = "495ba9cd-5168-4236-b8dd-6c5f8b7527c0";
    String assistantID = "2df2181f-e99b-440c-9b5a-45a9423e3365";
    String assistantOvertimeId = "52676105-5c28-47d9-a562-2f44b01ba967";
    @Mock
    private PPURepository ppuRepository;
    @Mock
    private RDORepository rdoRepository;
    @InjectMocks
    private BMPlatformHandler handler;
    @Mock
    private BMRepository repository;
    @Mock private OvertimeProjectionService overtimeProjectionServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should fetch By Platform BM calculation correctly")
    void fetchByPlatformBM() {
        var ppu = createFakePPUToBMTimeline();

        var start = LocalDate.of(2025, 10, 1);
        var end = LocalDate.of(2025, 10, 3);
        var project = 44L;

        var rdo1 = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3))
        ));
        rdo1.setPlatform("P-TESTE");
        rdo1.setDate(start);
        var rdos = List.of(rdo1);
        var range = new DateRange(start, end);
        var bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(rdos);
        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(rdo1.getServices(), List.of(
                        RDOServiceEntity.builder().serviceID(assistantOvertimeId).valueMeasured(3.0).build()
                )));

        var response = handler.fetchAll( "BMID");

        assertEquals(2, response.size());

        var platformItem = response.stream()
                .filter(item -> "P-TESTE".equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();

        assertNotNull(platformItem);
        assertEquals("P-TESTE", platformItem.getPlatform());
        assertTrue(platformItem.getTotalQuantity() >= 0);
        assertNotNull(platformItem.getTotalValue());

        var summaryItem = response.stream()
                .filter(item -> item.getPlatform().contains("Plataformas"))
                .findFirst()
                .orElseThrow();

        assertNotNull(summaryItem);
        assertTrue(summaryItem.getPlatform().contains("Plataformas"));
    }

    @Test
    @DisplayName("Should calculate By Platform BM for multiple platforms correctly")
    void fetchByPlatformBM_multiPlatform() {
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

        var rdo2 = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3))
        ));
        rdo2.setPlatform("P-PROD");
        rdo2.setDate(start);
        var range = new DateRange(start, end);

        var bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();

        var rdos = List.of(rdo1, rdo2);
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE", "P-PROD"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(rdos);

        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(List.of(
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(4)),
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3))
                ), List.of(
                        RDOServiceEntity.builder().serviceID(assistantOvertimeId).valueMeasured(7.0).build()
                )));

        var response = handler.fetchAll("BMID");

        assertEquals(3, response.size());

        var pTesteItem = response.stream()
                .filter(item -> "P-TESTE".equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();

        assertNotNull(pTesteItem);
        assertEquals("P-TESTE", pTesteItem.getPlatform());
        assertTrue(pTesteItem.getTotalQuantity() >= 0);
        assertTrue(pTesteItem.getTotalValue().compareTo(BigDecimal.ZERO) >= 0);

        var pProdItem = response.stream()
                .filter(item -> "P-PROD".equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();

        assertNotNull(pProdItem);
        assertEquals("P-PROD", pProdItem.getPlatform());
        assertTrue(pProdItem.getTotalQuantity() >= 0);
        assertTrue(pProdItem.getTotalValue().compareTo(BigDecimal.ZERO) >= 0);

        var summaryItem = response.stream()
                .filter(item -> item.getPlatform().contains("Plataformas"))
                .findFirst()
                .orElseThrow();

        assertNotNull(summaryItem);
        assertEquals("2 Plataformas", summaryItem.getPlatform());
        
        var expectedTotalQuantity = pTesteItem.getTotalQuantity() + pProdItem.getTotalQuantity();
        BigDecimal expectedTotalValue = pTesteItem.getTotalValue().add(pProdItem.getTotalValue());
        
        assertEquals(expectedTotalQuantity, summaryItem.getTotalQuantity());
        assertEquals(0, expectedTotalValue.compareTo(summaryItem.getTotalValue()));
    }

    @Test
    @DisplayName("Should handle empty RDOs correctly")
    void fetchByPlatformBM_noRDOs() {
        var ppu = createFakePPUToBMTimeline();
        var start = LocalDate.of(2025, 10, 1);
        var end = LocalDate.of(2025, 10, 1);
        var project = 44L;
        var range = new DateRange(start, end);

        var bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(List.of());
        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(List.of(), List.of()));

        var response = handler.fetchAll( "BMID");

        assertEquals(2, response.size());

        var platformItem = response.stream()
                .filter(item -> "P-TESTE".equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();

        assertEquals(0, platformItem.getTotalQuantity());
        assertEquals(0, platformItem.getTotalValue().compareTo(BigDecimal.ZERO));

        var summaryItem = response.stream()
                .filter(item -> item.getPlatform().contains("Plataformas"))
                .findFirst()
                .orElseThrow();

        assertEquals(0, summaryItem.getTotalQuantity());
        assertEquals(0, summaryItem.getTotalValue().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should calculate values correctly ignoring contractual adjustments")
    void fetchByPlatformBM_ignoreContractualAdjustments() {
        var ppu = createFakePPUToBMTimeline();
        var start = LocalDate.of(2025, 10, 1);
        var end = LocalDate.of(2025, 10, 1);
        var project = 44L;
        var rdo = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO)
        ));
        rdo.setPlatform("P-TESTE");
        rdo.setDate(start);
        var range = new DateRange(start, end);

        var bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));

        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE"), RDOStatusOP.APPROVED, start, end))
                .thenReturn(List.of(rdo));

        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(rdo.getServices(), List.of()));


        var response = handler.fetchAll( "BMID");

        var platformItem = response.stream()
                .filter(item -> "P-TESTE".equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();


        assertNotNull(platformItem.getTotalValue());
        assertTrue(platformItem.getTotalQuantity() >= 0);
        
        assertEquals(1, platformItem.getTotalQuantity());
    }
}