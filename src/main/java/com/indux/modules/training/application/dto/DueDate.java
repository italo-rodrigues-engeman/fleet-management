package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record DueDate (
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
        Integer total,
        @JsonProperty("finalizado")
        Integer completed,
        String status
){
}
