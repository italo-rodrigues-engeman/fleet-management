package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Strategy particional geral: a H.N (adicional noturno) e a H.E (hora extra) dessa linha 
 * valem x% para cada colaborador nessa linha.
 * 
 * Aplica o percentual tanto para hora extra quanto para adicional noturno.
 */
@Component
public class GeneralPartitionalStrategy implements TimeStrategy {

    @Override
    public TimeStrategyType type() {
        return TimeStrategyType.GENERAL_PARTITIONAL;
    }

    @Override
    public EnumSet<TimeStrategyPhase> phases() {
        return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION);
    }

    @Override
    public EnumSet<TimeStrategyKind> kinds() {
        return EnumSet.of(TimeStrategyKind.MUTATE);
    }
}

