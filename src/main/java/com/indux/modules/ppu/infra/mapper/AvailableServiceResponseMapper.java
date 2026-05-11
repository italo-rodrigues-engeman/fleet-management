package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.application.dtos.response.AvailableServiceDTO;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AvailableServiceResponseMapper {
    AvailableServiceResponseMapper INSTANCE = Mappers.getMapper(AvailableServiceResponseMapper.class);

    @Mapping(source = "genericNumber", target = "numero")
    @Mapping(source = "ppuNumber", target = "numeroPPU")
    @Mapping(source = "name", target = "nome")
    @Mapping(source = "employees", target = "colaboradores")
    AvailableServiceDTO toResponse(ServiceLine entity);

}
