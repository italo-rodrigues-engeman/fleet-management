package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InstituinResponseDTO(
        String id,
        @JsonProperty("nome")
        String name,
        String cnpj,
        @JsonProperty("unidades")
        List<UnitDTO> units,
        @JsonProperty("treinamentosId")
        List<String> trainingId,
        @JsonProperty("treinamentosNome")
        List<String> trainingName,
        @JsonProperty("propostas")
        List<ProposalResponseDTO> proposals
) {
}
