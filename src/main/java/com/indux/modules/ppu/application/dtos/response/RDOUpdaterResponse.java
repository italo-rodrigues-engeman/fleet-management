package com.indux.modules.ppu.application.dtos.response;

import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;

public record RDOUpdaterResponse(
        RDOEntity rdo,
        PPUResponse ppu
) {
}
