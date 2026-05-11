package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Strategy particional para adicional noturno: o adicional noturno dessa linha vale x% 
 * para cada colaborador nessa linha.
 * 
 * Exemplo: se há 2 colaboradores e 8 horas de adicional noturno com 50% de distribuição,
 * cada colaborador recebe 8 * 0.5 = 4 horas de adicional noturno.
 */
@Component
public class PremiumNightPartitional implements TimeStrategy {
    @Override public TimeStrategyType type() { return TimeStrategyType.PREMIUM_NIGHT_PARTITIONAL; }

    @Override public EnumSet<TimeStrategyPhase> phases() {
        return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION);
    }

    @Override public EnumSet<TimeStrategyKind> kinds() { 
        return EnumSet.of(TimeStrategyKind.MUTATE); 
    }
}

