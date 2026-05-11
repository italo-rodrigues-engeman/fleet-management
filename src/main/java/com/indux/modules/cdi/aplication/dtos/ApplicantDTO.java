package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ApplicantDTO(
        @JsonProperty("nome") @NotNull(message = "Item obriagatório") String name,
        @JsonProperty("cargo") @NotNull(message = "Item obriagatório") String role,
        @JsonProperty("matricula") @NotNull(message = "Item obriagatório") String registration,
        @JsonProperty("cpf") @NotNull(message = "Item obriagatório") String cpf,
        @JsonProperty("regional") @NotNull(message = "Item obriagatório") String branch,
        @JsonProperty("contrato") @NotNull(message = "Item obriagatório") String contract,
        @JsonProperty("telefone1") @NotNull(message = "Item obriagatório") String phone1,
        @JsonProperty("telefone2") String phone2,
        @JsonProperty("email_particular") @NotNull(message = "Item obriagatório") String email1,
        @JsonProperty("email_comercial") String email2
) {
}
