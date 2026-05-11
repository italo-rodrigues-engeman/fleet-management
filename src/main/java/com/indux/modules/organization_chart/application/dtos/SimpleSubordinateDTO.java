package com.indux.modules.organization_chart.application.dtos;

import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;

import java.util.List;

public record SimpleSubordinateDTO(
        Long id,
        String sigla,
        String cargo,
        OrganizationType tipo,
        List<SimpleContractDTO> contratos,
        List<SimpleProjectDTO> projetos,
        SubordinadoParentInfo subordinadoA
) { 
    public record SubordinadoParentInfo(
            Long id,
            String sigla,
            String cargo,
            OrganizationType tipo
    ) { }
}
