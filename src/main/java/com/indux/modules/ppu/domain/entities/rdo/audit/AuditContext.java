package com.indux.modules.ppu.domain.entities.rdo.audit;

import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;

import java.time.LocalDate;
import java.util.List;

public record AuditContext(
        LocalDate date, String platform, PPUEntity ppu, RDOEntity rdo, List<SAMCRow> samcRows
) {
}
