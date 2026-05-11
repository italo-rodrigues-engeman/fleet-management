package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.util.List;

public record FiliaisHCMResponse(
        @JsonProperty("filialHCM")
        List<String> filialHCM,
        @JsonProperty("tipo")
        String type,
        @JsonProperty("obrigatoriedade")
        String mandatory,
        @JsonProperty("cliente")
        String client,
        @JsonProperty("clienteNome")
        String clientName,
        @JsonProperty("norma")
        String norm,
        @JsonProperty("publico")
        String target,
        @JsonProperty("prazo")
        Double time,
        String link,
        @JsonProperty("anexoPegar")
        AttachmentEntity attachment,
        @JsonProperty("idFilial")
        List<Integer> idFilial,
        @JsonProperty("nomeFilial")
        List<String> nomeFilial,
        @JsonProperty("idProjeto")
        List<Long> idProjeto,
        @JsonProperty("nomeProjeto")
        List<String> nomeProjeto,
        @JsonProperty("idContrato")
        List<Long> idContrato,
        @JsonProperty("nomeContrato")
        List<String> nomeContrato,
        @JsonProperty("idRegional")
        List<Long> idRegional,
        @JsonProperty("nomeRegional")
        List<String> nomeRegional,
        @JsonProperty("cargaHoraria")
        Double workload,
        @JsonProperty("cargaHorariaRemunerada")
        Double duration,
        @JsonProperty("validade")
        Double validity,
        @JsonProperty("exigencia")
        Double requeriment,
        @JsonProperty("notaMinima")
        Double minimun,
        @JsonProperty("provisorio")
        String provisional,
        @JsonProperty("provisorioValidade")
        Double provisionalValidity,
        @JsonProperty("cargaHorariaReciclagem")
        Double workloadRecycling,
        @JsonProperty("cargaHorariaRemuneradaReciclagem")
        Double durationRecycling,
        @JsonProperty("validadeReciclagem")
        Double validityRecycling

) {
}
