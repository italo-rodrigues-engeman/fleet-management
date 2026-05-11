package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

public record NiOrganizationReturn(
        String id,
        @JsonProperty("partimonio")
        String heritage,
        @JsonProperty
        CalibrationStandardReturn calibrationStandard,
        @JsonProperty("regional")
        List<OrganizationDTO> branch,
        @JsonProperty("contrato")
        List<ContractDTO> contract,
        @JsonProperty("projeto")
        List<ProjectDTO> project,
        @JsonProperty("observacao")
        String observation,
        @JsonProperty("calibracao")
        List<CalibrationReturn> calibration
) {
}