package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.entity.*;

import java.time.LocalDate;
import java.util.List;

public record CommercialInteractionsResponse (
        @JsonProperty("id") String id,
        @JsonProperty("tipo_de_contato") String contactType,
        @JsonProperty("data") LocalDate date,
        @JsonProperty("descricao") String description,
        @JsonProperty("atencao") String attention,
        @JsonProperty("janela_de_oportunidade") String windowOfOpportunity,
        @JsonProperty("status") Boolean status,
        @JsonProperty("cliente") String client,
        @JsonProperty("unidade") String unit,
        @JsonProperty("contato") String lead,
        @JsonProperty("representante_engeman") List<EngemanAgent> engemanAgent,
        @JsonProperty("alertas") List<Alert> alerts,
        @JsonProperty("data_cadastro") LocalDate registrationDate,
        @JsonProperty("responsavel_cadastro") String responsibleUser
        ){}
