package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

public record RequiredTrainingStraring(
        @Field("id_treinamento") @JsonProperty("idTreinamento") String trainingId,
        @Field("partir") @JsonProperty("partir") LocalDate starting
) {
}
