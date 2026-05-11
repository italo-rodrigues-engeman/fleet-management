package com.indux.modules.ppu.application.services.rdo;

import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Component
public class OvertimeCalculator {

    public static Duration calculate(List<EmployeeOvertime> overtimes) {
        if (overtimes == null || overtimes.isEmpty()) return Duration.ZERO;

        var total = Duration.ZERO;
        for (var overtime : overtimes) {
            if (overtime != null && overtime.initial() != null && overtime.end() != null) {
                total = calcOvertime(overtime, total);
            }
        }
        return total;
    }

    private static Duration calcOvertime(EmployeeOvertime overtime, Duration total) {
        var start = overtime.initial();
        var end = overtime.end();

        if (!end.isAfter(start)) {
            total = total.plus(Duration.between(start, LocalTime.MAX).plusNanos(1));
            total = total.plus(Duration.between(LocalTime.MIDNIGHT, end));
        } else {
            total = total.plus(Duration.between(start, end));
        }
        return total;
    }
}


