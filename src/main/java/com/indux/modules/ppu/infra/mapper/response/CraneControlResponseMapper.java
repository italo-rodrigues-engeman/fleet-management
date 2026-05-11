package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.CraneControlResponse;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CraneControlResponseMapper {
    CraneControlResponse toResponse(CraneControl entity);
}

