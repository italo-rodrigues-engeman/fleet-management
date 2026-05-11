package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.lines.SteelCableLineResponse;
import com.indux.modules.ppu.domain.entities.ppu.SteelCableLine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SteelCableLineResponseMapper {
    SteelCableLineResponse toResponse(SteelCableLine entity);
}

