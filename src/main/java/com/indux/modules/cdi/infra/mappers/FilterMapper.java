package com.indux.modules.cdi.infra.mappers;

import com.indux.modules.cdi.aplication.dtos.FilterCdiDTO;
import com.indux.modules.cdi.aplication.dtos.FilterCdiTranslateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FilterMapper {

    @Mapping(source = "tipo", target = "type")
    @Mapping(source = "abrangencia", target = "scope")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "etapa", target = "stage")
    @Mapping(source = "filiais", target = "branches")
    FilterCdiTranslateDTO toEntity(FilterCdiDTO dto);
}