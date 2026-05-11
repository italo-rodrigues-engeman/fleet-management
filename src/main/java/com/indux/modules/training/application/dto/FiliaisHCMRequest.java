package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record FiliaisHCMRequest(
        @JsonProperty("filialHCM")
        List<String> filialHCM,
        @JsonProperty("tipo")
        String type,
        @JsonProperty("obrigatoriedade")
        String mandatory,
        @JsonProperty("cliente")
        String client,
        @JsonProperty("norma")
        String norm,
        @JsonProperty("publico")
        String target,
        @JsonProperty("prazo")
        Double time,
        String link,
        @JsonProperty("anexoCriar")
        MultipartFile file,
        @JsonProperty("subtitulo")
        String subtitle,
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
        @JsonProperty("subtituloReciclagem")
        String subtitleRecycling,
        @JsonProperty("cargaHorariaReciclagem")
        Double workloadRecycling,
        @JsonProperty("cargaHorariaRemuneradaReciclagem")
        Double durationRecycling,
        @JsonProperty("validadeReciclagem")
        Double validityRecycling
) {

}
