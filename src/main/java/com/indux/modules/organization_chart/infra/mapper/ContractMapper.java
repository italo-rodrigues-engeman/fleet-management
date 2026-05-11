package com.indux.modules.organization_chart.infra.mapper;

import com.indux.core.domain.model.employee.Filial;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.CreateContractDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.nome", target = "name")
    @Mapping(source = "dto.apelido", target = "nickname")
    @Mapping(source = "dto.os", target = "os")
    @Mapping(source = "dto.tipo", target = "type")
    @Mapping(source = "subordinate", target = "subordinate")
    @Mapping(source = "dto.hierarquia", target = "hierarchy")
    ContractEntity toEntity(CreateContractDTO dto, OrganizationEntity subordinate);
    
    @Mapping(source = "branch", target = "branch")
    ContractDTO toDto(ContractEntity dto);
}
