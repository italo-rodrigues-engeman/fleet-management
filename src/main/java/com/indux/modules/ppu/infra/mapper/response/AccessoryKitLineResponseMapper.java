package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.lines.AccessoryKitLineResponse;
import com.indux.modules.ppu.domain.entities.ppu.AccessoryKitLine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccessoryKitLineResponseMapper {
    AccessoryKitLineResponse toResponse(AccessoryKitLine entity);
}

