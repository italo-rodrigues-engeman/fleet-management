package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Properties(
        String id,
        @JsonProperty("nome") String name,
        Boolean status
) {
}