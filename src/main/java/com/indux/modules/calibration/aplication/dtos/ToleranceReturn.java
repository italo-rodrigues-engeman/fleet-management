package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;

import java.time.LocalDate;
import java.util.List;

public record ToleranceReturn(
        @JsonProperty("proprietarios")
        List<Properties> properties,
        @JsonProperty("regionais")
        List<OrganizationDTO> branch,
        @JsonProperty("contratos")
        List<ContractDTO> contract,
        @JsonProperty("projetos")
        List<ProjectDTO> project,
        @JsonProperty("valorTolerancia")
        Double valueTolerance
) {
}