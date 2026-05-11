package com.indux.modules.organization_chart.infra.mapper;

import com.indux.core.domain.model.employee.Employee;
import com.indux.modules.organization_chart.application.dtos.CreateOrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.cargo", target = "position")
    @Mapping(source = "dto.sigla", target = "acronym")
    @Mapping(source = "colaborador", target = "collaborator")
    @Mapping(source = "dto.observacao", target = "observation")
    @Mapping(source = "dto.ativo", target = "active")
    @Mapping(source = "subordinate", target = "subordinate")
    @Mapping(source = "dto.hierarquia", target = "hierarchy")
    @Mapping(source = "dto.tipo", target = "type")
    @Mapping(source = "dto.subTipo", target = "subType")
    OrganizationEntity toEntity(CreateOrganizationDTO dto, OrganizationEntity subordinate, Employee  colaborador);
    OrganizationDTO toDtoSingle(OrganizationEntity dto);
    List<OrganizationDTO> toDTO(List<OrganizationEntity> dtos);
}
