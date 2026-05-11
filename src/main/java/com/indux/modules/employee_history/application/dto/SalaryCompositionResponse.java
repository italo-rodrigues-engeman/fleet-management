package com.indux.modules.employee_history.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryCompositionResponse {

    @JsonProperty("matricula")
    private String registration;

    @JsonProperty("idFuncionrio")
    private UUID employeeId;

    @JsonProperty("filialIdHcm")
    private String filialIdHcm;

    @JsonProperty("nomeColaborador")
    private String employeeName;

    @JsonProperty("nomeCargo")
    private String cargoName;

    @JsonProperty("situacao")
    private String status;

    @JsonProperty("hierarquia")
    private EmployeeSummaryDTO.HierarchyInfo hierarchyInfo;

    @JsonProperty("salario")
    private Double salary;

    @JsonProperty("sobreAviso")
    private Double notice;

    @JsonProperty("periculosidade")
    private Double dangerousness;

    @JsonProperty("vr")
    private Double vr;

    @JsonProperty("flash")
    private Double flash;

    @JsonProperty("auxilioMoradia")
    private Double housingAllowance;

    @JsonProperty("premDesempContratual")
    private Double award;

    @JsonProperty("ajudaDeCusto")
    private Double costAllowance;

    @JsonProperty("premioDesempenho")
    private Double performanceAward;

    @JsonProperty("adicionalNoturno")
    private Double nightBonus;

    @JsonProperty("ahra")
    private Double ahra;
}
