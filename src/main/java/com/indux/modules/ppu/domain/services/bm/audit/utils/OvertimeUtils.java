package com.indux.modules.ppu.domain.services.bm.audit.utils;

import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;

import java.time.Duration;
import java.util.List;

public class OvertimeUtils {

    /**
     * Retorna o total de horas (em decimal) entre initial e end,
     * ajustando quando o intervalo cruza a meia-noite.
     *
     * @param overtime registro com início e fim da hora extra
     * @return horas trabalhadas (ex: 1.5 = 1h30m)
     */
    public static Double calculateOvertimeHours(EmployeeOvertime overtime) {
        if(overtime.initial() == null || overtime.end() == null) return 0.0;
        var start = overtime.initial();
        var end   = overtime.end();

        int startSec = start.toSecondOfDay();
        int endSec   = end.toSecondOfDay();
        if (endSec <= startSec) {
            endSec += 24 * 3600;
        }

        int diffSec = endSec - startSec;
        return diffSec / 3600.0;
    }



    public static Double calculateMultipleOvertimeHours(List<EmployeeOvertime> overtimeList){
        return overtimeList.stream()
                .mapToDouble(OvertimeUtils::calculateOvertimeHours)
                .sum();
    }


    public static Duration calculateOvertimeDuration(EmployeeOvertime overtime) {
        var hours = calculateOvertimeHours(overtime);
        long seconds = (long)(hours * 3600);
        return Duration.ofSeconds(seconds);
    }


}
