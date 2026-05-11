package com.indux.core.application.dto.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SimpleEmployeeDTO {
    @JsonProperty("matricula")
    private String registration;
    @JsonProperty("nome")
    private String name;
    @JsonProperty("cargo")
    private String position;
    @JsonProperty("cargoNome")
    private String positionName;
    @JsonProperty("SISPAT")
    private String sispat;
    @JsonProperty("contrato")
    private Map<String, Object> contract;
}
