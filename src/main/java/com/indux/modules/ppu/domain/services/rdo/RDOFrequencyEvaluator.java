package com.indux.modules.ppu.domain.services.rdo;

import com.indux.modules.ppu.domain.entities.item.RDOFrequency;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RDOFrequencyEvaluator {

    private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.getDefault());

    public List<LocalDate> findMissingDates(
            LocalDate start,
            LocalDate end,
            String platform,
            RDOFrequency frequency,
            List<RDOEntity> rdos) {
        
        if (frequency == RDOFrequency.ON_DEMAND) {
            return List.of();
        }

        List<LocalDate> rdoDates = rdos.stream()
                .filter(r -> platform.equals(r.getPlatform()))
                .map(RDOEntity::getDate)
                .filter(date -> !date.isBefore(start) && !date.isAfter(end))
                .sorted()
                .toList();

        return switch (frequency) {
            case DAILY -> findMissingDaily(start, end, rdoDates);
            case WEEKLY -> findMissingWeekly(start, end, rdoDates);
            case MONTHLY -> findMissingMonthly(start, end, rdoDates);
            default -> List.of();
        };
    }

    private List<LocalDate> findMissingDaily(LocalDate start, LocalDate end, List<LocalDate> rdoDates) {
        return start.datesUntil(end.plusDays(1))
                .filter(date -> !rdoDates.contains(date))
                .toList();
    }

    /**
     * Verifica RDOs faltantes para frequência SEMANAL (pelo menos 1 RDO por semana).
     * Uma semana é considerada completa se houver pelo menos 1 RDO em qualquer dia da semana.
     * Retorna o primeiro dia de cada semana que não possui RDO.
     * Regra a validar.
     */
    private List<LocalDate> findMissingWeekly(LocalDate start, LocalDate end, List<LocalDate> rdoDates) {
        Map<Integer, Boolean> weeksWithRdos = rdoDates.stream()
                .collect(Collectors.toMap(
                    date -> date.get(WEEK_FIELDS.weekOfWeekBasedYear()) * 10000 + date.getYear(),
                    date -> true,
                    (existing, replacement) -> true
                ));

        List<LocalDate> missingWeeks = new ArrayList<>();
        LocalDate current = start;
        
        while (!current.isAfter(end)) {
            int weekKey = current.get(WEEK_FIELDS.weekOfWeekBasedYear()) * 10000 + current.getYear();
            if (!weeksWithRdos.containsKey(weekKey)) {
                LocalDate firstDayOfWeek = current.with(WEEK_FIELDS.dayOfWeek(), 1);
                if (!firstDayOfWeek.isBefore(start)) {
                    missingWeeks.add(firstDayOfWeek);
                }
            }
            current = current.plusWeeks(1).with(WEEK_FIELDS.dayOfWeek(), 1);
            if (current.isAfter(end)) break;
        }
        
        return missingWeeks;
    }

    /**
     * Verifica RDOs faltantes para frequência MENSAL (pelo menos 1 RDO por mês).
     * Um mês é considerado completo se houver pelo menos 1 RDO em qualquer dia do mês.
     * Retorna apenas o primeiro dia de cada mês faltante.
     * Regra a validar.
     */
    private List<LocalDate> findMissingMonthly(LocalDate start, LocalDate end, List<LocalDate> rdoDates) {
        Map<String, Boolean> monthsWithRdos = rdoDates.stream()
                .collect(Collectors.toMap(
                    date -> date.getYear() + "-" + String.format("%02d", date.getMonthValue()),
                    date -> true,
                    (existing, replacement) -> true
                ));

        List<LocalDate> missingMonths = new ArrayList<>();
        LocalDate current = start.withDayOfMonth(1);
        
        while (!current.isAfter(end)) {
            String monthKey = current.getYear() + "-" + String.format("%02d", current.getMonthValue());
            if (!monthsWithRdos.containsKey(monthKey)) {
                missingMonths.add(current);
            }
            current = current.plusMonths(1).withDayOfMonth(1);
        }
        
        return missingMonths;
    }
}

