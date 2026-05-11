package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;

import java.time.LocalDate;
import java.util.List;

public record CalibrationAll (
        String id,
        Long autoIncrementId,
        @JsonProperty("patrimonio")
        String heritage,
        @JsonProperty("equipamento")
        Equipment equipment,
        @JsonProperty("regional")
        List<OrganizationDTO> branch,
        @JsonProperty("contrato")
        List<ContractDTO> contract,
        @JsonProperty("projeto")
        List<ProjectDTO> project,
        @JsonProperty("situacao")
        Boolean situation,
        @JsonProperty("ultimaData")
        LocalDate calibrationDate,
        @JsonProperty("status")
        String calibrationStatus,
        @JsonProperty("proximaCalibracao")
        LocalDate nextCalibration

){
}