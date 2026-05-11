package com.indux.modules.ppu.application.dtos.response.competence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record CompetenceResponse(
        @JsonProperty("competencia") String competence,
        @JsonProperty("dataInicio") LocalDate start,
        @JsonProperty("dataFim") LocalDate end,
        @JsonProperty("quantidadeFechada") Integer quantidadeFechada
) {
}
