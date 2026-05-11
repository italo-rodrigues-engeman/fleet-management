package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.missing.MissingRDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RDOToMissingRDOMapper {
    RDOToMissingRDOMapper INSTANCE = Mappers.getMapper(RDOToMissingRDOMapper.class);

    @Mapping(target = "rdo", expression = "java(String.valueOf(entity.getSequentialId()))")
    @Mapping(target = "contract", expression = "java(getProjectName(entity))")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "platform", target = "platform")
    @Mapping(source = "regionalNome", target = "regional")
    MissingRDO toMissingRDO(RDOEntity entity);

    default String getProjectName(RDOEntity entity) {
        Object projectName = entity.getContract().get("projectName");
        return projectName != null ? projectName.toString() : "Desconhecido";
    }
}
