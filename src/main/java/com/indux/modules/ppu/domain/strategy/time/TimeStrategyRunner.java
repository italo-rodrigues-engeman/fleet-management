package com.indux.modules.ppu.domain.strategy.time;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Component
public class TimeStrategyRunner {

    private final List<TimeStrategy> strategies;

    public TimeStrategyRunner(List<TimeStrategy> strategies) {
        var copy = new ArrayList<>(strategies == null ? List.of() : strategies);
        copy.sort(AnnotationAwareOrderComparator.INSTANCE);
        this.strategies = Collections.unmodifiableList(copy);
    }

    public Result run(TimeStrategyPhase phase, ProjectionContext ctx, List<RDOServiceEntity> base, List<ServiceLine> ppuServices) {
        if (base == null || base.isEmpty()) return new Result(List.of(), List.of());

        var working = base.stream()
                .filter(Objects::nonNull)
                .map(RDOServiceEntity::copy)
                .collect(Collectors.toCollection(ArrayList::new));

        var lines = ppuServices == null ? List.<ServiceLine>of() : ppuServices;
        var allowed = ctx == null ? ProjectionContext.AUDIT.allowedKinds() : ctx.allowedKinds();

        var virtuals = new ArrayList<RDOServiceEntity>();

        for (var st : strategies) {
            if (st == null) continue;
            if (!st.phases().contains(phase)) continue;

            var kinds = st.kinds();
            if (kinds == null || kinds.isEmpty()) continue;

            if (allowed.contains(TimeStrategyKind.MUTATE) && kinds.contains(TimeStrategyKind.MUTATE)) {
                st.mutate(working, lines);
            }

            if (allowed.contains(TimeStrategyKind.PRODUCE_VIRTUAL) && kinds.contains(TimeStrategyKind.PRODUCE_VIRTUAL)) {
                var produced = st.produceVirtuals(working, lines);
                if (produced != null && !produced.isEmpty()) {
                    var toAdd = produced.stream().filter(Objects::nonNull).toList();
                    if (!toAdd.isEmpty()) {
                        virtuals.addAll(toAdd);
                        working.addAll(toAdd);
                    }
                }
            }
        }

        return new Result(working, virtuals);
    }

    public Result run(List<RDOServiceEntity> base, List<ServiceLine> ppuServices, TimeStrategyPhase phase) {
        return run(phase, ProjectionContext.SAVE, base, ppuServices);
    }

    public record Result(List<RDOServiceEntity> working, List<RDOServiceEntity> virtuals) {}
}