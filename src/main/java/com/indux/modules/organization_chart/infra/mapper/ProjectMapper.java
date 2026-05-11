package com.indux.modules.organization_chart.infra.mapper;

import com.indux.modules.organization_chart.application.dtos.CreateProjectDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mega.cusInReduzido", source = "mega")
    @Mapping(target = "hcm.ccId", source = "hcm")
    @Mapping(target = "filial", ignore = true)
    @Mapping(target = "subordinate", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "ativo", source = "ativo")
    @Mapping(target = "type", source = "tipo")
    @Mapping(target = "branch.branchId", source = "filialMega")
    ProjectEntity toEntity(CreateProjectDTO dto);
    
    @Mapping(target = "active", source = "ativo")
    @Mapping(target = "branchMega", source = "branch")
    @Mapping(target = "tipo", source = "type")
    ProjectDTO toDto(ProjectEntity entity);

}
    