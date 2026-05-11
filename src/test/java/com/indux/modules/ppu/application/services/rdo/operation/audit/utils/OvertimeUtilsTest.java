package com.indux.modules.ppu.application.services.rdo.operation.audit.utils;

import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import com.indux.modules.ppu.domain.services.bm.audit.utils.OvertimeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OvertimeUtilsTest {

    @Test
    @DisplayName("Should count 2.0h in a daytime interval of 10:00–12:00")
    void shouldCountCorrectly(){
     var overtime = new EmployeeOvertime(LocalTime.of(10, 00), LocalTime.of(12, 00));
     var calculate = OvertimeUtils.calculateOvertimeHours(overtime);
     assertEquals(2.0, calculate);
    }

    @Test
    @DisplayName("Should count 2.75h in a daytime interval of 22:30–1:15")
    void shouldCountCorrectlyMidnightCross(){
        var overtime = new EmployeeOvertime(LocalTime.of(22, 30), LocalTime.of(1, 15));
        var calculate = OvertimeUtils.calculateOvertimeHours(overtime);
        assertEquals(2.75, calculate);
    }

    @Test
    @DisplayName("Should return 24h if the time is the same.")
    void testCalculateOvertimeHours_SameStartEnd() {
        var overtime = new EmployeeOvertime(LocalTime.of(8, 0), LocalTime.of(8, 0));
        var calculate = OvertimeUtils.calculateOvertimeHours(overtime);
        assertEquals(24.0, calculate);
    }

    @Test
    @DisplayName("Should return 1h30min -> 90 minutes")
    void testCalculateOvertimeDuration_Daytime() {
        var overtime = new EmployeeOvertime(LocalTime.of(9, 15), LocalTime.of(10, 45));
        var duration = OvertimeUtils.calculateOvertimeDuration(overtime);
        assertEquals(Duration.ofMinutes(90), duration);
    }

    @Test
    @DisplayName("Should return 3h30min in midnight")
    void testCalculateOvertimeDuration_MidnightCross() {
        EmployeeOvertime overtime = new EmployeeOvertime(LocalTime.of(23, 0), LocalTime.of(2, 30));
        Duration duration = OvertimeUtils.calculateOvertimeDuration(overtime);
        assertEquals(Duration.ofHours(3).plusMinutes(30), duration);
    }
}