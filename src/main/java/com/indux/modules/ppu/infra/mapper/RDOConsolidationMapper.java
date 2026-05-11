package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import com.indux.modules.ppu.application.dtos.response.competence.RDOConsolidationResponse;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RDOConsolidationMapper {
    RDOConsolidationResponse toResponse(ConsolidationRecord record);
    List<RDOConsolidationResponse> toResponseList(Collection<ConsolidationRecord> records);
}