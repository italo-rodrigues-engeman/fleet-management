package com.indux.modules.employee_history.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Optional;

public record CreateAlert(
        @JsonProperty("nomeDivergente") String nameDivergent,
        @JsonProperty("competencia") Optional<String> competence,
        @JsonProperty("tipo") String type,
        @JsonProperty("obs") String observation
) {
}
