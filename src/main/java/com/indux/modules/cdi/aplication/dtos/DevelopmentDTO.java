package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.cdi.domain.entities.models.StatusAction;

import java.util.Date;
import java.util.List;

public record DevelopmentDTO(
        @JsonProperty("titulo") String title,
        @JsonProperty("dataInicio")Date startDate,
        @JsonProperty("tempoPrevisto") Integer expectedTime,
        @JsonProperty("responsavel") List<EvaluatorDTO>  evaluator,
        @JsonProperty("detalhe")List<DetailActionDTO> detail,
        StatusAction status
        ) {
}
