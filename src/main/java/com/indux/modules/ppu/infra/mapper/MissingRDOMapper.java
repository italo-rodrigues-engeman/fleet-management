package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.domain.entities.rdo.missing.MissingRDO;
import com.indux.modules.ppu.presentation.dtos.MissingRDOResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MissingRDOMapper {
    MissingRDOMapper INSTANCE = Mappers.getMapper(MissingRDOMapper.class);


    @Mapping(source = "date", target = "data")
    @Mapping(source = "platform", target = "plataforma")
    @Mapping(source = "contract", target = "nomeContrato")
    @Mapping(source = "contractId", target = "contratoId")
    MissingRDOResponse toResponse(MissingRDO entity);


}
