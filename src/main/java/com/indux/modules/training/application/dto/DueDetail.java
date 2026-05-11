package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record DueDetail(
        UUID id,
        @JsonProperty("matricula")
        String registation,
        @JsonProperty("nome")
        String name,
        @JsonProperty("funcao")
        String function,
        @JsonProperty("regional")
        String regional,
        @JsonProperty("projeto")
        String project,
        @JsonProperty("treinamento")
        String training,
        String status
){
}
