package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public record CompanySummary(@JsonProperty("id")
                             String id, @JsonProperty("nome")
                             String name, @JsonProperty("cpnj")
                             String cnpj, @JsonProperty("mercado")
                             String market, @JsonProperty("setor")
                             String sector, @JsonProperty("usuario_responsavel")
                             String responsibleUser, @JsonProperty("data_cadastro")
                             LocalDate registrationDate, @JsonProperty("status")
                             Boolean status) {

}
