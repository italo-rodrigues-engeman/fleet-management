package com.indux.modules.crm.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.entity.Commission;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record EngemanAgentRequest(
        @JsonProperty("nome") String nome,
        @JsonProperty("cpf") String cpf,
        @JsonProperty("cep") String cep,
        @JsonProperty("endereco") String endereco,
        @JsonProperty("estado") String estado,
        @JsonProperty("cidade") String cidade,
        @JsonProperty("email_principal") String email_principal,
        @JsonProperty("email_alternativo") String email_alternativo,
        @JsonProperty("telefone_principal") String telefone_principal,
        @JsonProperty("telefone_alternativo") String telefone_alternativo,
        @JsonProperty("comissoes") List<Commission> comissoes,
        @JsonProperty("status") Boolean status
) {
}
