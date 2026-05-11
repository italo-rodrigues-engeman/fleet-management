package com.indux.modules.ppu.domain.entities.rdo.consolidation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder @AllArgsConstructor @NoArgsConstructor
public class EmployeeConsolidation {
    @JsonProperty("matricula")
    private String registration;
    @JsonProperty("status")
    private String event;
    @JsonProperty("nome")
    private String name;
    @JsonProperty("referencia")
    private String reference;
    @JsonProperty("valor")
    private BigDecimal value;
    @JsonProperty("plataforma")
    private String platform;
    @JsonProperty("competencia")
    private String competence;
}
