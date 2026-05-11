package com.indux.modules.cdi.infra.mappers;

import com.indux.modules.cdi.aplication.dtos.CreateCdiDTO;
import com.indux.modules.cdi.aplication.dtos.GetAllCdiDTO;
import com.indux.modules.cdi.aplication.dtos.GetIdCdiDTO;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CdiMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(source = "solicitante", target = "applicant")
    @Mapping(source = "tipo", target = "type")
    @Mapping(source = "abrangencia", target = "scope")
    @Mapping(source = "complexidade", target = "complexity")
    @Mapping(source = "tempoPrevisto", target = "previstTime")
    @Mapping(source = "descricao", target = "description")
    @Mapping(source = "independente", target = "independent")
    @Mapping(source = "detalhe", target = "details")
    @Mapping(source = "problema", target = "problem")
    @Mapping(source = "resultado", target = "result")
    @Mapping(source = "resultadoEspecifico", target = "especificResult")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "etapa", target = "stage")
    @Mapping(source = "titulo", target = "title")
    CdiEntity toEntity(CreateCdiDTO dto);
    GetIdCdiDTO toDTO(CdiEntity entity);
    List<GetAllCdiDTO> toDTOs(List<CdiEntity> entities);
}
