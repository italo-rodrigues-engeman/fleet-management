package com.indux.modules.employee_history.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("competencia")
    private Date competence;

    @JsonProperty("filialHcm")
    private Long filialHcm;

    @JsonProperty("matricula")
    private String registration;

    @JsonProperty("dataPagamento")
    private Date paymentDate;

    @JsonProperty("nomeEvento")
    private String eventName;

    @JsonProperty("descEvento")
    private String eventDescription;

    @JsonProperty("valor")
    private Double value;

    private UUID employeeId;

    @JsonProperty("nomeColaborador")
    private String employeeName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("nomeCargo")
    private String positionName;
}

