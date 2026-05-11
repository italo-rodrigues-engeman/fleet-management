package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy para mover colaborador: colaborador que tem hora extra deve ser jogado para outra linha.
 * Similar ao OvertimeAnotherLineStrategy, mas focado no colaborador específico.
 */
@Component
public class MoveOutStrategy implements TimeStrategy {
    @Override public TimeStrategyType type() { return TimeStrategyType.MOVE_OUT; }
    @Override public EnumSet<TimeStrategyPhase> phases() { return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION); }
    @Override public EnumSet<TimeStrategyKind> kinds() { return EnumSet.of(TimeStrategyKind.MUTATE); }

    @Override
    public void mutate(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        if (working == null || working.isEmpty() || ppuServices == null || ppuServices.isEmpty()) return;

        var byId = ppuServices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        for (var svc : working) {
            if (svc == null) continue;
            if (!hasOvertime(svc)) continue;

            var current = byId.get(svc.getServiceID());
            if (current == null) continue;

            if (current.getTimeStrategy() != type()) continue;

            var parentId = current.getParentId();
            if (parentId == null || parentId.isBlank()) continue;

            var parent = byId.get(parentId);
            if (parent == null) continue;

            svc.setServiceID(parent.getId());
            svc.setServiceNumber(parent.getGenericNumber());
            svc.setServiceName(parent.getName());
        }
    }

    private static boolean hasOvertime(RDOServiceEntity svc) {
        var d = svc.getOvertimeHourTotais();
        if (d != null && !d.isZero()) return true;

        var list = svc.getOvertimes();
        if (list == null || list.isEmpty()) return false;

        return list.stream()
                .filter(Objects::nonNull)
                .anyMatch(o -> o.initial() != null && o.end() != null && endAfterStartOrCrossMidnight(o.initial(), o.end()));
    }

    private static boolean endAfterStartOrCrossMidnight(LocalTime start, LocalTime end) {
        return !end.equals(start);
    }

}
