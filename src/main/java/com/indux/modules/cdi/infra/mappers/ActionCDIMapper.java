package com.indux.modules.cdi.infra.mappers;

import com.indux.modules.cdi.aplication.dtos.ActionCDIDTO;
import com.indux.modules.cdi.aplication.dtos.CreateActionDTO;
import com.indux.modules.cdi.domain.entities.mongo.ActionCDIEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActionCDIMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "prazo", target = "term")
    @Mapping(source = "responsavel", target = "evaluator")
    @Mapping(source = "pontos", target = "points")
    //@Mapping(source = "desenvolvimento", target = "development")
    ActionCDIEntity toEntity(CreateActionDTO entity);
    ActionCDIDTO toDTO(ActionCDIEntity entity);
    List<ActionCDIDTO>  toDTOList(List<ActionCDIEntity> dtos);

}
