package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Strategy para linha especial: essa linha não possui hora extra.
 * Todas as horas extras são zeradas.
 */
@Component
@Primary
public class SpecialLineStrategy implements TimeStrategy {
    @Override public TimeStrategyType type() { return TimeStrategyType.SPECIAL_LINE; }

    @Override public EnumSet<TimeStrategyPhase> phases() {
        return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION);
    }

    @Override public EnumSet<TimeStrategyKind> kinds() { return EnumSet.of(TimeStrategyKind.MUTATE); }

}

