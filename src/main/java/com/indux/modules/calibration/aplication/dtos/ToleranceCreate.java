package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ToleranceCreate {
    @JsonProperty("regionaisId")
    private List<Long> branchId;
    @JsonProperty("contratosId")
    private List<Long> contractId;
    @JsonProperty("projetosId")
    private List<Long> projectId;
    @JsonProperty("valorTolerancia")
    private Double valueTolerance;
}