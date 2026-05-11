package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Dossier(
        @JsonProperty("treinamentoNome")
        String trainingName,
        List<DossierClass> dossierClasses
        ) {
}
