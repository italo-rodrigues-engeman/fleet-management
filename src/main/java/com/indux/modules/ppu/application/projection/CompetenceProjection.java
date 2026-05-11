package com.indux.modules.ppu.application.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public interface CompetenceProjection {
    @JsonProperty("id") String getId();
    @JsonProperty("criadoEm") LocalDate getCreatedAt();
    @JsonProperty("competencia") String getCompetence();
    @JsonProperty("aprovador") ApproverInfo getApprover();
    @JsonProperty("projeto") Long getProject();
    @JsonProperty("dataInicio") LocalDate getInitialDate();
    @JsonProperty("dataFinal") LocalDate getFinalDate();
    interface ApproverInfo {
        String getNome();
    }
}
