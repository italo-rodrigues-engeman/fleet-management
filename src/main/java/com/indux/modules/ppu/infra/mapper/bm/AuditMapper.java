package com.indux.modules.ppu.infra.mapper.bm;

import com.indux.modules.ppu.domain.entities.rdo.audit.AuditDivergence;
import com.indux.modules.ppu.presentation.dtos.audit.AuditResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditMapper {
    AuditResponse toResponse(AuditDivergence entity);
    List<AuditResponse> toResponses(List<AuditDivergence> divergences);
}
