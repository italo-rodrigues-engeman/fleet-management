package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.application.dtos.response.audit.AuditMIOResponse;
import com.indux.modules.ppu.domain.entities.rdo.audit.MioDivergence;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MioDivergenceMapper {

    AuditMIOResponse toResponse(MioDivergence entity);
    List<AuditMIOResponse> toResponses(List<MioDivergence> entities);
}
