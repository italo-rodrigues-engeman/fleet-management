package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CBODTO(
        String id,
        Integer codCBO,
        @JsonProperty("cargosRelacionados") List<RelatedPosition> relatedPosition,
        @JsonProperty("nomeCBO")String nameCBO,
        @JsonProperty("descricao")String description,
        @JsonProperty("atividades") String activity,
        @JsonProperty("formacao") String formation,
        @JsonProperty("condicoes")String codintion,
        @JsonProperty("notas") String notes,
        @JsonProperty("recursos") String resorces,
        @JsonProperty("glossario") String glossary
        ) {
}
