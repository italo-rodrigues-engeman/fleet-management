package com.indux.modules.modulo_mega.application.dto.items;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record MegaItemResponse(
        Integer id,
        @JsonProperty("label") String name,
        @JsonProperty("grupo") Integer groupId,
        @JsonProperty("") LocalDate creationDate,
        @JsonProperty("") String createdBy,
        @JsonProperty("") String updatedBy,
        @JsonProperty("") String unitOfMeasure,
        @JsonProperty("") String status,
        @JsonProperty("desativacao") LocalDate inactivationForecast
       ) {
}
