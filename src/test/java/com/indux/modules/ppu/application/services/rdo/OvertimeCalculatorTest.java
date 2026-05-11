package com.indux.modules.ppu.application.services.rdo;

import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OvertimeCalculatorTest {
    @Test
    @DisplayName("Should return 0h when there are no overtime entries")
    public void testZeroHour(){
        var response = OvertimeCalculator.calculate(null);
        assertEquals(Duration.ZERO, response);

        List<EmployeeOvertime> items = Arrays.asList(null, null, null);
        response = OvertimeCalculator.calculate(items);
        assertEquals(Duration.ZERO, response);
    }

    @Test
    @DisplayName("Should sum multiple overtime periods correctly, including crossing midnight")
    public void testCalculate(){
        var hour = new EmployeeOvertime(LocalTime.of(5, 0), LocalTime.of(7, 0));
        var items = Arrays.asList(hour, null, null);
        var response = OvertimeCalculator.calculate(items);
        assertEquals(Duration.ofHours(2), response);

        var night = new EmployeeOvertime(LocalTime.of(19, 0), LocalTime.of(22, 0));
        items = Arrays.asList(hour, night, null);
        response = OvertimeCalculator.calculate(items);
        assertEquals(Duration.ofHours(5), response);

        var threeHr = new EmployeeOvertime(LocalTime.of(23, 0), LocalTime.of(1, 0));
        items = Arrays.asList(hour, night, threeHr);
        response = OvertimeCalculator.calculate(items);
        assertEquals(Duration.ofHours(7), response);
    }

    @Test
    @DisplayName("Should calculate overtime correctly when crossing midnight only")
    public void testOnlyHRThree(){
        var threeHr = new EmployeeOvertime(LocalTime.of(23, 0), LocalTime.of(1, 0));
        var items = Arrays.asList(null, null, threeHr);
        var response = OvertimeCalculator.calculate(items);
        assertEquals(Duration.ofHours(2), response);
    }


}