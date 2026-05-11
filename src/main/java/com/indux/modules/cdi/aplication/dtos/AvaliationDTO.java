package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public record AvaliationDTO(
        @JsonProperty("gravidade") Integer severity,
        @JsonProperty("urgencia") Integer  urgency,
        @JsonProperty("tendencia") Integer trend,
        @JsonProperty("observacao") String observation,
        @JsonProperty("avaliacaoAt") Date avaliationAt,
        @JsonProperty("criterios") List<CriteriaDTO> criteria,
        @JsonProperty("avaliador") EvaluatorDTO evaluator
) {
}
