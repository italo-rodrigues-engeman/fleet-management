package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record LeadResponse(@JsonProperty("id")
                           String id, @JsonProperty("nome")
                           String name, @JsonProperty("cargo")
                           String function, @JsonProperty("nivel_dicisorio")
                           String decisionMakeLevel, @JsonProperty("unidade")
                           String  unit, @JsonProperty("email_principal")
                           String mainEmail, @JsonProperty("email_alternativo")
                           String alternativeEmail, @JsonProperty("numero_principal")
                           String mainNumber, @JsonProperty("numero_alternativo")
                           String alternativeNumber, @JsonProperty("conta_do_linkedin")
                           String linkedinAccount, @JsonProperty("observacoes")
                           String observations, @JsonProperty("status")
                           Boolean status, @JsonProperty("responsavel_cadastro")
                           String responsibleUser, @JsonProperty("data_registro")
                           LocalDate registrationDate

) {}
