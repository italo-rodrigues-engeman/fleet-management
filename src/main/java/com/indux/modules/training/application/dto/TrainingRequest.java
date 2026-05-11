package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrainingRequest(
        String id,
        @JsonProperty("nome")
        String name,
        Boolean status,
        @JsonProperty("descricao")
        String description,
        @JsonProperty("efetiva")
        Double effective,
        List<FiliaisHCMRequest> filiais,
        List<String> prestserv,
        List<String> myDrake
) {
}
