package com.indux.modules.ppu.domain.entities.rdo.audit;

import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AuditBatchContext(
        LocalDate start,
        LocalDate end,
        PPUEntity ppu,
        List<RDOEntity> rdos,
        List<SAMCRow> samcRows,
        Map<LocalDate, Map<String, List<RDOEntity>>> rdoByDatePlatform,
        Map<LocalDate, Map<String, List<SAMCRow>>> samcByDatePlatform
) {
    public AuditBatchContext {
        rdos = rdos == null ? List.of() : List.copyOf(rdos);
        samcRows = samcRows == null ? List.of() : List.copyOf(samcRows);
        rdoByDatePlatform = rdoByDatePlatform == null ? Map.of() : Map.copyOf(rdoByDatePlatform);
        samcByDatePlatform = samcByDatePlatform == null ? Map.of() : Map.copyOf(samcByDatePlatform);
    }
    public AuditBatchContext withRdos(List<RDOEntity> newRdos) {
        return new AuditBatchContext(start, end, ppu, newRdos, samcRows, rdoByDatePlatform, samcByDatePlatform);
    }

    public AuditBatchContext withSlices(
            Map<LocalDate, Map<String, List<RDOEntity>>> rdoMap,
            Map<LocalDate, Map<String, List<SAMCRow>>> samcMap
    ) {
        return new AuditBatchContext(start, end, ppu, rdos, samcRows, rdoMap, samcMap);
    }
}
