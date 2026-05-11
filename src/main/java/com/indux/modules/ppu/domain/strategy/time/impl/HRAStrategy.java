package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HRAStrategy implements TimeStrategy {
    @Override public TimeStrategyType type() { return TimeStrategyType.HRA; }
    @Override public EnumSet<TimeStrategyPhase> phases() { return EnumSet.of(TimeStrategyPhase.CREATE); }
    @Override public EnumSet<TimeStrategyKind> kinds() { return EnumSet.of(TimeStrategyKind.MUTATE); }

    /*
    ** O colaborador NÃO pode tirar mais de <b>1 hora</b> de extra. Ele receberá apenas 1h extra sempre.
     */
    @Override
    public void mutate(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        if (working == null || working.isEmpty() || ppuServices == null || ppuServices.isEmpty()) return;
        var lineId = ppuServices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));
        for (var svc : working) {
            if (svc == null) continue;

            if (Boolean.TRUE.equals(svc.getDisposicao())) continue;

            var line = lineId.get(svc.getServiceID());
            if (line == null) continue;

            if (line.getTimeStrategy() != type()) continue;

            var  quantityOvertime = 1L;
            svc.setOvertimeHourTotais(Duration.ofHours(quantityOvertime));
        }
    }
}
