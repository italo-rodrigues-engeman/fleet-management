package com.indux.modules.training.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UnitDTO(
        @JsonProperty("uf")
        String state,
        @JsonProperty("cidade")
        String city,
        @JsonProperty("rua")
        String street,
        @JsonProperty("numero")
        String number,
        @JsonProperty("complemento")
        String complement,
        @JsonProperty("bairo")
        String neighborhood,
        @JsonProperty("contato")
        String contact,
        @JsonProperty("telefone")
        String phone,
        @JsonProperty("site")
        String link,
        @JsonProperty("treinamentoAtivo")
        List<Boolean> activeTraining
) {
}
