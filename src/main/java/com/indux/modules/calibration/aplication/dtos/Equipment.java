package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Equipment(
        String id,
        @JsonProperty("nome") String name,
        Boolean status,
        @JsonProperty("fabricante") Manufacturer manufacturer,
        @JsonProperty("fabricanteId") String manufacturerId,
        @JsonProperty("proprietarios") List<Properties> properties,
        @JsonProperty("proprietariosIds") List<String> propertiesIds
) {
}