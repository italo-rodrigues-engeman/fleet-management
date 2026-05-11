package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.domain.strategy.time.ProjectionContext;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategyRunner;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OvertimeProjectionServiceImpl implements OvertimeProjectionService {
    private final TimeStrategyRunner runner;

    public OvertimeProjectionServiceImpl(TimeStrategyRunner runner) {
        this.runner = runner;
    }

    @Override
    public ProjectionResult project(List<RDOServiceEntity> base, List<ServiceLine> ppuServices) {
        return project(base, ppuServices, ProjectionContext.AUDIT);
    }

    @Override
    public ProjectionResult project(List<RDOServiceEntity> base, List<ServiceLine> ppuServices, ProjectionContext ctx) {
        var result = runner.run(TimeStrategyPhase.PROJECTION, ctx, base, ppuServices);
        return new ProjectionResult(result.working(), result.virtuals());
    }
}
