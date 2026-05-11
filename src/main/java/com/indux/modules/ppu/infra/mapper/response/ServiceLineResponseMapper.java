package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.lines.ServiceLineResponse;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceLineResponseMapper {
    ServiceLineResponse toResponse(ServiceLine entity);
}

