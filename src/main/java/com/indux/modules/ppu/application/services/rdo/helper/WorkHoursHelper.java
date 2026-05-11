package com.indux.modules.ppu.application.services.rdo.helper;

import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import com.indux.modules.ppu.application.dtos.response.CalculatorResponse;
import com.indux.modules.ppu.application.services.rdo.OvertimeCalculator;
import com.indux.modules.ppu.application.services.rdo.PremiumNightCalculator;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkHoursHelper {
    private static final double THRESHOLD = 0.501d;

    public CalculatorResponse compute(ShiftSchedule schedule, List<EmployeeOvertime> overtimes) {
        if (schedule == null || schedule.getHorarioInicial() == null || schedule.getHorarioFinal() == null) {
            return CalculatorResponse.builder()
                    .nightShiftPremium(Duration.ZERO)
                    .normalHours(Duration.ZERO)
                    .hourTotais(Duration.ZERO)
                    .overtimeHourTotais(Duration.ZERO)
                    .build();
        }

        var start  = schedule.getHorarioInicial();
        var end    = schedule.getHorarioFinal();
        var extras = (overtimes == null) ? List.<EmployeeOvertime>of() : overtimes;

        var shiftDuration  = duration(start, end);
        var filtered       = filterOverlaps(start, end, extras);
        var extrasDuration = OvertimeCalculator.calculate(filtered);
        var totalWorked    = shiftDuration.plus(extrasDuration);

        var overtime = totalWorked.compareTo(Duration.ofHours(12)) > 0
                ? totalWorked.minus(Duration.ofHours(12))
                : Duration.ZERO;

        var ranges = filtered.stream()
                .map(e -> new PremiumNightCalculator.TimeRange(e.initial(), e.end()))
                .toList();

        var nightPure = PremiumNightCalculator.calculateNightHoursDiurno(start, end, ranges);


        long nightSec  = nightPure.getSeconds();
        long workedSec = Math.max(1, totalWorked.getSeconds());
        boolean extended = (nightSec / (double) workedSec) > THRESHOLD;

        var night = extended
                ? PremiumNightCalculator.calculateNightHoursExtendedFrom22(start, end, ranges)
                : nightPure;

        var normalHours = (totalWorked.compareTo(Duration.ofHours(12)) >= 0)
                ? Duration.ofHours(11)
                : shiftDuration;

        return CalculatorResponse.builder()
                .nightShiftPremium(night)
                .normalHours(normalHours)
                .hourTotais(totalWorked)
                .overtimeHourTotais(overtime)
                .build();
    }



    public void applyTo(RDOServiceEntity service) {
        var res = compute(service.getSchedule(), service.getOvertimes());
        service.setNightShiftPremium(res.getNightShiftPremium());
        service.setHourTotais(res.getHourTotais());
        service.setNormalHours(res.getNormalHours());
        service.setOvertimeHourTotais(res.getOvertimeHourTotais());
    }

    private static Duration duration(LocalTime start, LocalTime end) {
        if (!end.isAfter(start))
            return Duration.between(start, LocalTime.MAX).plusNanos(1).plus(Duration.between(LocalTime.MIDNIGHT, end));
        return Duration.between(start, end);
    }

    private static List<EmployeeOvertime> filterOverlaps(LocalTime shiftStart, LocalTime shiftEnd, List<EmployeeOvertime> extras) {
        if (extras == null || extras.isEmpty()) return List.of();
        int base = toSec(shiftStart);
        int[] shiftAligned = toAlignedRange(shiftStart, shiftEnd, base);

        var result = new ArrayList<EmployeeOvertime>();
        for (var e : extras) {
            if (e == null || e.initial() == null || e.end() == null) continue;
            var r = toAlignedRange(e.initial(), e.end(), base);
            if (!(r[0] >= shiftAligned[0] && r[1] <= shiftAligned[1])) result.add(e);
        }
        return result;
    }

    private static int[] toAlignedRange(LocalTime sT, LocalTime eT, int base) {
        int s = toSec(sT), e = toSec(eT);
        if (s < base) s += 86400;
        if (e < s) e += 86400;
        return new int[]{s, e};
    }

    private static int toSec(LocalTime t) { return t.getHour()*3600 + t.getMinute()*60 + t.getSecond(); }
}