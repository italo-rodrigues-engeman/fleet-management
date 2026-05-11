package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Manufacturer(
        String id,
        @JsonProperty("nome") String name,
        Boolean status,
        String url
) {
}