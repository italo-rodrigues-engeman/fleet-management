package com.indux.modules.organization_chart.application.dtos;

import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;

import java.util.List;

public record SubordinatesByTypeDTO(
        OrganizationType type,
        List<SimpleSubordinateDTO> subordinates
) { }
