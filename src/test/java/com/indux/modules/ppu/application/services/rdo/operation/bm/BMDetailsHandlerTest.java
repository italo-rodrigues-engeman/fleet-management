package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.response.bm.BMServiceReportItem;
import com.indux.modules.ppu.domain.entities.item.MeasurementPeriod;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.indux.modules.ppu.application.services.rdo.operation.bm.BMFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class BMDetailsHandlerTest {

    String supervisorID = "495ba9cd-5168-4236-b8dd-6c5f8b7527c0";

    @Mock
    PPURepository ppuRepository;
    @Mock
    RDORepository rdoRepository;
    @Mock
    private BMRepository repository;
    @Mock private OvertimeProjectionService overtimeProjectionService;
    @Spy
    BMMapper mapper = Mappers.getMapper(BMMapper.class);
    @InjectMocks
    BMDetailsHandler handler;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should calculate BM report correctly for specific platform")
    void fetchItem_shouldCalculateReport_forSpecificPlatform() {
        var platform = "P-TESTE";
        var startDate = LocalDate.of(2025, 10, 1);
        var endDate = LocalDate.of(2025, 10, 5);
        var project = 44L;
        var ppu = createFakePPUToBMTimeline();

        var range = new DateRange(startDate, endDate);
        var bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .status(DocumentStatus.APROVADO)
                .build();
        ppu.setMeasurementPeriod(new MeasurementPeriod(1, 31));

        var rdo1 = createFakeRDOForBM(List.of(
                createFakeRDOServiceForBM(supervisorID, null)
        ));
        var rdo2 = createFakeRDOForBM(List.of(
                createFakeRDOServiceForBM(supervisorID, null)
        ));
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));
        when(rdoRepository.findAllByPlatformAndStatusOPAndDateBetween(platform, RDOStatusOP.APPROVED, startDate, endDate))
                .thenReturn(List.of(rdo1, rdo2));

        when(overtimeProjectionService.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionService.ProjectionResult(
                        List.of(
                                createFakeRDOServiceForBM(supervisorID, null),
                                createFakeRDOServiceForBM(supervisorID, null)
                        ),
                       List.of()
                ));

        List<BMServiceReportItem> result = handler.fetchItem("BMID", platform);
        assertEquals(6, result.size());

        BMServiceReportItem supervisorReport = result.stream()
                .filter(item -> supervisorID.equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(supervisorID, supervisorReport.getId());
        assertEquals(50.0, supervisorReport.getQtdExpected());
        assertEquals(2.0, supervisorReport.getQtdReal());
        assertEquals(BigDecimal.valueOf(25000.00).setScale(2, RoundingMode.UNNECESSARY), supervisorReport.getValueExpected());
        assertEquals(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.UNNECESSARY), supervisorReport.getValueReal());
    }

    @Test
    @DisplayName("Should calculate BM report correctly for all platforms")
    void fetchItem_shouldCalculateReport_forAllPlatforms() {
        var platform = "*";
        var startDate = LocalDate.of(2025, 10, 1);
        var endDate = LocalDate.of(2025, 10, 3);
        var project = 44L;
        var ppu = createMultiPlatformPPU();

        var range = new DateRange(startDate, endDate);
        var bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();
        ppu.setMeasurementPeriod(new MeasurementPeriod(1, 31));

        var supervisorID = "495ba9cd-5168-4236-b8dd-6c5f8b7527c0";
        var assistantID = "2df2181f-e99b-440c-9b5a-45a9423e3365";
        
        var rdo1 = createFakeRDOForBM(List.of(
                createFakeRDOServiceForBM(supervisorID, null),
                createFakeRDOServiceForBM(assistantID, null)
        ));
        rdo1.setPlatform("P-TESTE");
        
        var rdo2 = createFakeRDOForBM(List.of(
                createFakeRDOServiceForBM(supervisorID, null),
                createFakeRDOServiceForBM(assistantID, null)
        ));
        rdo2.setPlatform("P-PROD");
        when(repository.findById("BMID")).thenReturn(Optional.ofNullable(bm));
        when(ppuRepository.findByProjectIdAndStatus(project, DocumentStatus.ABERTO))
                .thenReturn(Optional.of(ppu));
        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of("P-TESTE", "P-PROD"), RDOStatusOP.APPROVED, startDate, endDate))
                .thenReturn(List.of(rdo1, rdo2));

        when(overtimeProjectionService.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionService.ProjectionResult(
                        List.of(
                                createFakeRDOServiceForBM(supervisorID, null),
                                createFakeRDOServiceForBM(assistantID, null),
                                createFakeRDOServiceForBM(supervisorID, null),
                                createFakeRDOServiceForBM(assistantID, null)
                        ),
                        List.of()
                ));


        List<BMServiceReportItem> result = handler.fetchItem("BMID", platform);
        assertEquals(6, result.size());

        BMServiceReportItem supervisorReport = result.stream()
                .filter(item -> supervisorID.equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(45.0, supervisorReport.getQtdExpected());
        assertEquals(2.0, supervisorReport.getQtdReal());
        
        BMServiceReportItem assistantReport = result.stream()
                .filter(item -> assistantID.equals(item.getId()))
                .findFirst()
                .orElseThrow();
        
        assertEquals(54.0, assistantReport.getQtdExpected());
        assertEquals(2.0, assistantReport.getQtdReal());
    }
}