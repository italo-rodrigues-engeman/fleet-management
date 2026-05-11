package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.response.bm.BMTimeline;
import com.indux.modules.ppu.domain.entities.bm.BMContext;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.indux.modules.ppu.application.services.rdo.operation.bm.BMFixtures.createFakePPUToBMTimeline;
import static com.indux.modules.ppu.application.services.rdo.operation.bm.BMFixtures.createMultiPlatformPPU;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class BMTimelineHandlerTest {
    String supervisorID = "495ba9cd-5168-4236-b8dd-6c5f8b7527c0";
    String assistantID = "2df2181f-e99b-440c-9b5a-45a9423e3365";
    String supervisorOvertimeId = "15e1d2c7-3a79-4470-bcbe-d9801ba206c6";
    String assistantOvertimeId = "52676105-5c28-47d9-a562-2f44b01ba967";

    @Mock
    private PPURepository ppuRepository;
    @Mock
    private RDORepository rdoRepository;
    @Mock
    private BMRepository repository;

    @InjectMocks
    private BMTimelineHandler handler;

    @Mock private OvertimeProjectionServiceImpl overtimeProjectionServiceImpl;

    @Spy
    private BMMapper mapper = Mappers.getMapper(BMMapper.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should build overtime entries for supervisor and for the max overtime service")
    void fetchBMTimeline_shouldBuildOvertimeEntries() {
        var ppu = createFakePPUToBMTimeline();


        var date = LocalDate.of(2025, 10, 3);
        var platform = "P-TESTE";
        var projectId = 44L;

        var rdo = BMFixtures.createFakeRDOForBM(List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ofHours(5)),
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ofHours(2)),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(5)),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(5)),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(5)),
                BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(5))
        ));
        rdo.setPlatform(platform);
        rdo.setDate(date);

        var bm = BMEntity.builder()
                .id("BMID")
                .period(new DateRange(date, date))
                .projectId(projectId)
                .build();

        when(repository.findById("BMID")).thenReturn(Optional.of(bm));
        when(ppuRepository.findByProjectIdAndStatus(projectId, DocumentStatus.ABERTO)).thenReturn(Optional.of(ppu));
        when(rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                List.of(platform), RDOStatusOP.APPROVED, date, date))
                .thenReturn(List.of(rdo));
        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(rdo.getServices(), List.of(
                        RDOServiceEntity.builder().serviceID(supervisorOvertimeId).valueMeasured(7.0).build(),
                        RDOServiceEntity.builder().serviceID(assistantOvertimeId).valueMeasured(20.0).build())));
        var response = handler.fetchBMTimeline(platform, "BMID");

        var byId = response.stream().collect(Collectors.toMap(BMTimeline::getId, t -> t));

        var supervisorTimeline = requireTimeline(byId, supervisorID);

        var supervisorOvertimeTimeline = findOvertimeChildTimeline(response, supervisorTimeline);

        assertEquals(
                BMFixtures.createDailyEntry(date, date.getDayOfMonth(), 7.0),
                supervisorOvertimeTimeline.getTimeline().getFirst()
        );

        var maxOvertimeTimeline = response.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsOvertimeService()))
                .max(Comparator.comparingDouble(t -> (double) t.getTimeline().getFirst().quantity()))
                .orElseThrow(() -> new AssertionError("No overtime timelines found"));

        assertEquals(
                BMFixtures.createDailyEntry(date, date.getDayOfMonth(), 20.0),
                maxOvertimeTimeline.getTimeline().getFirst()
        );
    }

    private static BMTimeline requireTimeline(Map<String, BMTimeline> byId, String id) {
        var t = byId.get(id);
        if (t == null) throw new AssertionError("Timeline not found: id=" + id);
        return t;
    }

    private static BMTimeline findOvertimeChildTimeline(List<BMTimeline> all, BMTimeline parent) {
        var childrenNumbers = Optional.ofNullable(parent.getChildren())
                .orElseThrow(() -> new AssertionError("Parent has no children configured: id=" + parent.getId()));

        return all.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsOvertimeService()))
                .filter(t -> Objects.equals(t.getPlatform(), parent.getPlatform()))
                .filter(t -> childrenNumbers.contains(t.getNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Overtime child not found for parent id=" + parent.getId() +
                                " platform=" + parent.getPlatform() +
                                " children=" + childrenNumbers
                ));
    }

    @Test
    @DisplayName("Should perform BM Timeline correctly")
    void performBMTimeline() {
        var ppu = createFakePPUToBMTimeline();
        var platform = "P-TESTE";

        var timelines = handler.performBMTimeline(ppu, platform);

        assertEquals(6, timelines.size());
        assertFalse(timelines.getFirst().getIsOvertimeService());
    }

    @Test
    @DisplayName("Should build daily entry and timeline correctly (new daily service timeline)")
    void buildDailyServiceTimeline() {

        var services = List.of(
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ofHours(5)),
                BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ofHours(2))
        );

        var rdo = BMFixtures.createFakeRDOForBM(services);
        rdo.setPlatform("P-TESTE");
        var rdos = List.of(rdo);

        var start = LocalDate.of(2025, 10, 3);

        var timelines = new ArrayList<BMTimeline>();
        var timeline1 = BMFixtures.createBMTimeLine1();
        timeline1.setPlatform("P-TESTE");
        var timeline2 = BMFixtures.createBMTimeLineChild2();
        timeline2.setPlatform("P-TESTE");
        timelines.add(timeline1);
        timelines.add(timeline2);

        var virtualOvertime = RDOServiceEntity.builder()
                .serviceID(timeline2.getId())
                .valueMeasured(7.0)
                .build();

        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(services, List.of(virtualOvertime)));

        var context = new BMContext(start, start, "P-TESTE", 44L);
        context.setDaysInQuery(1);

        var response = handler.buildDailyServiceTimeline(timelines, rdos, context, List.of());

        assertEquals(2, response.size());

        var firstItem = response.getFirst();
        var overtimeService = response.get(1);

        assertEquals(4.0, firstItem.getTimeline().getFirst().quantity());
        assertEquals(7.0, overtimeService.getTimeline().getFirst().quantity());
    }

    @Test
    @DisplayName("Should return 0 quantities when no RDOs are present")
    void buildDailyTimeLine_noRDO_zeroes() {
        var ppu = createFakePPUToBMTimeline();
        var timelines = handler.performBMTimeline(ppu, "P-TESTE");

        var day = LocalDate.of(2025, 10, 4);
        var rdos = List.<RDOEntity>of();
        var context = new BMContext(day, day, "P-TESTE", 44L);
        context.setDaysInQuery(1);
        when(overtimeProjectionServiceImpl.project(anyList(), anyList()))
                .thenReturn(new OvertimeProjectionServiceImpl.ProjectionResult(List.of(), List.of()));
        var out = handler.buildDailyServiceTimeline(timelines, rdos, context, List.of());
        out.forEach(t -> assertEquals(0.0, t.getTimeline().getFirst().quantity()));
    }

    @Test
    @DisplayName("Should fetch BM Timeline for all platforms correctly")
    void fetchBMTimeline_allPlatforms() {
        var ppu = createMultiPlatformPPU();

        var start = LocalDate.of(2025, 10, 3);
        var end = LocalDate.of(2025, 10, 3);
        var platform = "*";
        var project = 44L;
        var range = new DateRange(start, end);

        BMEntity bm = BMEntity.builder()
                .id("BMID")
                .period(range)
                .projectId(project)
                .build();

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
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(3)),
                        BMFixtures.createFakeRDOServiceForBM(supervisorID, Duration.ZERO),
                        BMFixtures.createFakeRDOServiceForBM(assistantID, Duration.ofHours(2))
                ), List.of(
                        RDOServiceEntity.builder().serviceID(supervisorOvertimeId).valueMeasured(0.0).build(),
                        RDOServiceEntity.builder().serviceID(assistantOvertimeId).valueMeasured(5.0).build()
                )));


        var response = handler.fetchBMTimeline(platform, "BMID");

        assertEquals(6, response.size());

        response.forEach(timeline -> assertEquals("ALL", timeline.getPlatform()));

        var supervisorTimeline = response.stream()
                .filter(t -> supervisorID.equals(t.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(15, supervisorTimeline.getQtdExpected());

        assertEquals(2.0, supervisorTimeline.getTimeline().getFirst().quantity());
    }
}