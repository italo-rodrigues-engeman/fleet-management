package com.indux.modules.ppu.domain.services.rdo;

import com.indux.modules.ppu.domain.entities.item.RDOFrequency;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RDOFrequencyEvaluatorTest {

    private RDOFrequencyEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RDOFrequencyEvaluator();
    }

    @Test
    @DisplayName("Should return empty list for ON_DEMAND frequency")
    void findMissingDates_OnDemand_ShouldReturnEmpty() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        String platform = "PRA-1";
        List<RDOEntity> rdos = List.of();

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.ON_DEMAND, rdos);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all missing dates for DAILY frequency")
    void findMissingDates_Daily_ShouldFindAllMissingDates() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 5);
        String platform = "PRA-1";
        
        RDOEntity rdo1 = createRDO("PRA-1", LocalDate.of(2024, 1, 2));
        RDOEntity rdo2 = createRDO("PRA-1", LocalDate.of(2024, 1, 4));
        List<RDOEntity> rdos = List.of(rdo1, rdo2);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertEquals(3, result.size());
        assertTrue(result.contains(LocalDate.of(2024, 1, 1)));
        assertTrue(result.contains(LocalDate.of(2024, 1, 3)));
        assertTrue(result.contains(LocalDate.of(2024, 1, 5)));
    }

    @Test
    @DisplayName("Should return empty list when all dates have RDOs for DAILY frequency")
    void findMissingDates_Daily_AllDatesPresent_ShouldReturnEmpty() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 3);
        String platform = "PRA-1";
        
        RDOEntity rdo1 = createRDO("PRA-1", LocalDate.of(2024, 1, 1));
        RDOEntity rdo2 = createRDO("PRA-1", LocalDate.of(2024, 1, 2));
        RDOEntity rdo3 = createRDO("PRA-1", LocalDate.of(2024, 1, 3));
        List<RDOEntity> rdos = List.of(rdo1, rdo2, rdo3);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter RDOs by platform for DAILY frequency")
    void findMissingDates_Daily_ShouldFilterByPlatform() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 3);
        String platform = "PRA-1";
        
        RDOEntity rdo1 = createRDO("PRA-1", LocalDate.of(2024, 1, 1));
        RDOEntity rdo2 = createRDO("PRA-2", LocalDate.of(2024, 1, 2)); // Different platform
        List<RDOEntity> rdos = List.of(rdo1, rdo2);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertEquals(2, result.size());
        assertTrue(result.contains(LocalDate.of(2024, 1, 2)));
        assertTrue(result.contains(LocalDate.of(2024, 1, 3)));
        assertFalse(result.contains(LocalDate.of(2024, 1, 1)));
    }

    @Test
    @DisplayName("Should find missing weeks for WEEKLY frequency")
    void findMissingDates_Weekly_ShouldFindMissingWeeks() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 21);
        String platform = "PRA-1";
        

        RDOEntity rdo = createRDO("PRA-1", LocalDate.of(2024, 1, 10));
        List<RDOEntity> rdos = List.of(rdo);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.WEEKLY, rdos);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }


    @Test
    @DisplayName("Should find missing months for MONTHLY frequency")
    void findMissingDates_Monthly_ShouldFindMissingMonths() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        String platform = "PRA-1";
        

        RDOEntity rdo = createRDO("PRA-1", LocalDate.of(2024, 2, 15));
        List<RDOEntity> rdos = List.of(rdo);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.MONTHLY, rdos);

        assertEquals(2, result.size());
        assertTrue(result.contains(LocalDate.of(2024, 1, 1)));
        assertTrue(result.contains(LocalDate.of(2024, 3, 1)));
        assertFalse(result.contains(LocalDate.of(2024, 2, 1)));
    }

    @Test
    @DisplayName("Should return empty list when all months have RDOs for MONTHLY frequency")
    void findMissingDates_Monthly_AllMonthsPresent_ShouldReturnEmpty() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        String platform = "PRA-1";
        
        RDOEntity rdo1 = createRDO("PRA-1", LocalDate.of(2024, 1, 15));
        RDOEntity rdo2 = createRDO("PRA-1", LocalDate.of(2024, 2, 15));
        RDOEntity rdo3 = createRDO("PRA-1", LocalDate.of(2024, 3, 15));
        List<RDOEntity> rdos = List.of(rdo1, rdo2, rdo3);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.MONTHLY, rdos);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should ignore RDOs outside date range")
    void findMissingDates_ShouldIgnoreRDOsOutsideRange() {
        LocalDate start = LocalDate.of(2024, 1, 5);
        LocalDate end = LocalDate.of(2024, 1, 7);
        String platform = "PRA-1";
        
        RDOEntity rdoBefore = createRDO("PRA-1", LocalDate.of(2024, 1, 1)); // Before range
        RDOEntity rdoAfter = createRDO("PRA-1", LocalDate.of(2024, 1, 10)); // After range
        RDOEntity rdoInRange = createRDO("PRA-1", LocalDate.of(2024, 1, 6)); // In range
        List<RDOEntity> rdos = List.of(rdoBefore, rdoAfter, rdoInRange);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertEquals(2, result.size());
        assertTrue(result.contains(LocalDate.of(2024, 1, 5)));
        assertTrue(result.contains(LocalDate.of(2024, 1, 7)));
        assertFalse(result.contains(LocalDate.of(2024, 1, 6)));
    }

    @Test
    @DisplayName("Should handle empty RDO list for DAILY frequency")
    void findMissingDates_Daily_EmptyRDOList_ShouldReturnAllDates() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 3);
        String platform = "PRA-1";
        List<RDOEntity> rdos = List.of();

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertEquals(3, result.size());
        assertTrue(result.contains(LocalDate.of(2024, 1, 1)));
        assertTrue(result.contains(LocalDate.of(2024, 1, 2)));
        assertTrue(result.contains(LocalDate.of(2024, 1, 3)));
    }

    @Test
    @DisplayName("Should handle single day range for DAILY frequency")
    void findMissingDates_Daily_SingleDayRange() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 1);
        String platform = "PRA-1";
        List<RDOEntity> rdos = List.of();

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertEquals(1, result.size());
        assertTrue(result.contains(LocalDate.of(2024, 1, 1)));
    }

    @Test
    @DisplayName("Should handle single day range with RDO present for DAILY frequency")
    void findMissingDates_Daily_SingleDayRangeWithRDO() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 1);
        String platform = "PRA-1";
        RDOEntity rdo = createRDO("PRA-1", LocalDate.of(2024, 1, 1));
        List<RDOEntity> rdos = List.of(rdo);

        List<LocalDate> result = evaluator.findMissingDates(start, end, platform, RDOFrequency.DAILY, rdos);

        assertTrue(result.isEmpty());
    }

    private RDOEntity createRDO(String platform, LocalDate date) {
        return RDOEntity.builder()
                .platform(platform)
                .date(date)
                .build();
    }
}

