package com.indux.modules.crm.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LeadRequest(
        @JsonProperty("nome") String name,
        @JsonProperty("cargo") String function,
        @JsonProperty("nivel_dicisorio") String decisionMakeLevel,
        @JsonProperty("email_principal") String mainEmail,
        @JsonProperty("email_alternativo") String alternativeEmail,
        @JsonProperty("numero_principal") String mainNumber,
        @JsonProperty("numero_alternativo") String alternativeNumber,
        @JsonProperty("conta_do_linkedin") String linkedinAccount,
        @JsonProperty("observacoes") String observations,
        @JsonProperty("status") Boolean status
        )
{}
