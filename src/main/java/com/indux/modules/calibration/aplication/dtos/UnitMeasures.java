package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnitMeasures(
        String id,
        @JsonProperty("nome") String name,
        Boolean status,
        @JsonProperty("medidaId") String measureId,
        @JsonProperty("medida") Measures measures,
        @JsonProperty("abreviacao") String abbreviation
) {
}