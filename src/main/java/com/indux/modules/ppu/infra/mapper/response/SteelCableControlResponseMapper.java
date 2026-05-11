package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.SteelCableControlResponse;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SteelCableControlResponseMapper {
    SteelCableControlResponse toResponse(SteelCableControl entity);
}

