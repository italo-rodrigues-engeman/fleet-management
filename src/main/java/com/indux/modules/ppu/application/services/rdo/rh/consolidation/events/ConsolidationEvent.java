package com.indux.modules.ppu.application.services.rdo.rh.consolidation.events;

import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface ConsolidationEvent {
    Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence, Boolean excelReport);
    Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence);
    String consolidate(YearMonth competence, Long project, LocalDate startDate, LocalDate endDate);

}
