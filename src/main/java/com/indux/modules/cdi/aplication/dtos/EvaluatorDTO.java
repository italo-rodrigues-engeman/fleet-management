package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluatorDTO(
        String cpf,
        @JsonProperty("nome") String name,
        @JsonProperty("maticula") String registration,
        @JsonProperty("cargo") String role
) {
}
