package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.clients.domain.model.Client;

public record HeadquartesDTO(
        String id,
        @JsonProperty("nome")
        String name,
        @JsonProperty("ac/oc")
        String ac,
        String filialHCM,
        @JsonProperty("filialNome")
        String nomeFilial,
        @JsonProperty("ambiente")
        String environment,
        @JsonProperty("clienteId")
        String client,
        Client cliente,
        Long idProjeto,
        String nomeProjeto,
        Long idContrato,
        String nomeContrato,
        Long idRegional,
        String nomeRegional
) {
}
