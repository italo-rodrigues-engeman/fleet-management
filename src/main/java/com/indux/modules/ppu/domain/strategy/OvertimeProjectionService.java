package com.indux.modules.ppu.domain.strategy;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.ProjectionContext;

import java.util.List;

public interface OvertimeProjectionService {
    ProjectionResult project(List<RDOServiceEntity> base, List<ServiceLine> ppuServices);

    ProjectionResult project(List<RDOServiceEntity> base, List<ServiceLine> ppuServices, ProjectionContext ctx);
   record ProjectionResult(List<RDOServiceEntity> working, List<RDOServiceEntity> virtuals) {}
}
