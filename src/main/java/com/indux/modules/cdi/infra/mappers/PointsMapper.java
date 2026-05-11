package com.indux.modules.cdi.infra.mappers;

import com.indux.modules.cdi.aplication.dtos.PointsDTO;
import com.indux.modules.cdi.aplication.dtos.UpdatePointsDTO;
import com.indux.modules.cdi.domain.entities.jpa.PointsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PointsMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "pontosAlto", target="highPoint")
    @Mapping(source = "pontosMedio", target="medioPoint")
    @Mapping(source = "pontosBaixo", target="lowPoint")
    PointsEntity toEntity(UpdatePointsDTO dto);
    List<PointsDTO> toDto(List<PointsEntity> dto);

}
