package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceQuantityResponse {
    @JsonProperty("numero_nota_fiscal")
    private Long invoiceNumber;

    @JsonProperty("quantidade")
    private Integer quantity;

    @JsonProperty("id_projeto")
    private Integer projectId;

    @JsonProperty("ap")
    private Integer ap;

    @JsonProperty("valor_unitario")
    private Integer unitValue;


}
