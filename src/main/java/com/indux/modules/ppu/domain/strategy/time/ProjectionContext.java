package com.indux.modules.ppu.domain.strategy.time;

import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;

import java.util.EnumSet;

public enum ProjectionContext {
    SAVE(EnumSet.of(TimeStrategyKind.MUTATE)),
    AUDIT(EnumSet.of(TimeStrategyKind.PRODUCE_VIRTUAL));

    private final EnumSet<TimeStrategyKind> allowedKinds;

    ProjectionContext(EnumSet<TimeStrategyKind> allowedKinds) {
        this.allowedKinds = allowedKinds;
    }

    public EnumSet<TimeStrategyKind> allowedKinds() {
        return allowedKinds;
    }
}