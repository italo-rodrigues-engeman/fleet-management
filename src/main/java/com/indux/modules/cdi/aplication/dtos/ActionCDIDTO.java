package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public record ActionCDIDTO(
        String id,
        @JsonProperty("cdiId") String cdiId,
        @JsonProperty("prazo")Date term,
        @JsonProperty("responsavel") ApplicantDTO evaluator,
        @JsonProperty("desenvolvimento")List<DevelopmentDTO> development,
        @JsonProperty("requisitos") RequestActionDTO request,
        @JsonProperty("aprovacao") RequestActionDTO approval,
        @JsonProperty("testes") DevelopmentDTO test,
        @JsonProperty("testesFinais") DevelopmentDTO finalTest,
        @JsonProperty("avaliacao") String avaliationDescription,
        @JsonProperty("razao") String reason,
        @JsonProperty("correcao") List<DevelopmentDTO> fixes,
        @JsonProperty("melhorias") List<DevelopmentDTO> improves,
        @JsonProperty("pontos") Integer points,
        @JsonProperty("titulo") String title,
        String status
        ) {

}
