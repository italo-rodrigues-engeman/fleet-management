package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy para mover hora extra: os valores de hora extra são jogados para outra linha.
 * As horas extras desta linha são zeradas e marcadas para serem movidas.
 */
@Component
public class OvertimeAnotherLineStrategy implements TimeStrategy {

    @Override
    public TimeStrategyType type() {
        return TimeStrategyType.OVERTIME_ANOTHER_LINE;
    }

    @Override
    public EnumSet<TimeStrategyPhase> phases() {
        return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION);
    }

    @Override
    public EnumSet<TimeStrategyKind> kinds() {
        return EnumSet.of(TimeStrategyKind.PRODUCE_VIRTUAL);
    }

    @Override
    public List<RDOServiceEntity> produceVirtuals(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        if (working == null || working.isEmpty() || ppuServices == null || ppuServices.isEmpty()) return List.of();

        var lineById = ppuServices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        var totals = new HashMap<String, Duration>();

        for (var svc : working) {
            if (svc == null) continue;

            var line = lineById.get(svc.getServiceID());
            if (line == null) continue;

            if (line.getTimeStrategy() != type()) continue;

            if (Boolean.TRUE.equals(line.getIsOvertimeService())) continue;

            var overtimeId = line.getOvertimeService();
            if (overtimeId == null || overtimeId.isBlank()) continue;

            var d = Optional.ofNullable(svc.getOvertimeHourTotais()).orElse(Duration.ZERO);
            if (d.isZero()) continue;

            totals.merge(overtimeId, d, Duration::plus);
        }

        if (totals.isEmpty()) return List.of();

        return totals.entrySet().stream()
                .map(e -> buildOvertimeVirtual(e.getKey(), e.getValue(), lineById))
                .toList();
    }

    private RDOServiceEntity buildOvertimeVirtual(String overtimeId, Duration total, Map<String, ServiceLine> lineById) {
        var overtimeLine = lineById.get(overtimeId);
        if (overtimeLine == null) {
            throw new ModuleFailure("Serviço de HORA EXTRA de id '" + overtimeId + "' não foi encontrado na PPU.");
        }

        double hours = total.toSeconds() / 3600.0;

        var rdo = new RDOServiceEntity();
        rdo.setServiceID(overtimeId);
        rdo.setServiceName(overtimeLine.getName());
        rdo.setServiceNumber(overtimeLine.getGenericNumber());
        rdo.setOvertimeHourTotais(total);
        rdo.setValueMeasured(hours);
        return rdo;
    }

}

