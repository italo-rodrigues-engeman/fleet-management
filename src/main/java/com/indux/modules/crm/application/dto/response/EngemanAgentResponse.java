package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.util.List;

public record EngemanAgentResponse(
        @JsonProperty("id") String id,
        @JsonProperty("nome") String name,
        @JsonProperty("cpf") String cpf,
        @JsonProperty("cep") String cep,
        @JsonProperty("endereco") String address,
        @JsonProperty("estado") String state,
        @JsonProperty("cidade") String city,
        @JsonProperty("email_principal") String mainEmail,
        @JsonProperty("email_alternativo") String alternativeEmail,
        @JsonProperty("telefone_principal") String mainNumber,
        @JsonProperty("telefone_alternativo") String alternativeNumber,
        @JsonProperty("status") Boolean status,
        @JsonProperty("comissoes") List<CommissionResponse> commissions,
        @JsonProperty("anexos") List<AttachmentEntity> attachments
        ) {
}
