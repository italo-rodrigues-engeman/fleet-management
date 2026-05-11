package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Measures (
        String id,
        @JsonProperty("nome") String name,
        Boolean status,
        @JsonProperty("descricao") String description
){
}