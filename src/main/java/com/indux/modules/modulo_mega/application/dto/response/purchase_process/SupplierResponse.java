package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponse {

    @JsonProperty("id_fornecedor")
    private Integer supplierCod;

    @JsonProperty("nome_fornecedor")
    private String supplierName;

    @JsonProperty("cnpj")
    private String cnpj;
}
