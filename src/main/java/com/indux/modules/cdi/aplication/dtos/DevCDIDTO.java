package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DevCDIDTO(
        @JsonProperty("requisitos") RequestActionDTO request,
        @JsonProperty("aprovacao") RequestActionDTO approval,
        @JsonProperty("desenvolvimento") List<DevelopmentDTO> development,
        @JsonProperty("testes") DevelopmentDTO test,
        @JsonProperty("testesFinais") DevelopmentDTO finalTest,
        @JsonProperty("avaliacao") String avaliationDescription,
        @JsonProperty("razao") String reason,
        @JsonProperty("correcao") List<DevelopmentDTO> fixes,
        @JsonProperty("melhorias") List<DevelopmentDTO> improves,
        String status
) {
}
