package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CriteriaDTO(
        String id,
        @JsonProperty("criterio") String criteria,
        @JsonProperty("descricao") String description,
        @JsonProperty("maxPontos") Integer maxPoints,
        @JsonProperty("pontos") Integer points
) {
}
