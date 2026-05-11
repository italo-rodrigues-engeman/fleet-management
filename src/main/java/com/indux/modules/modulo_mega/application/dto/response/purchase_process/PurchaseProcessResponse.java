package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PurchaseProcessResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("referencia")
    private String ref;

    @JsonProperty("solicitacoes")
    private List<RequestResponse> requests;

    @JsonProperty("pedidos")
    private List<PurchaseOrderResponse> order;

    @JsonProperty("aps")
    private List<Integer> ap;

    @JsonProperty("numeros_nota_fiscal")
    private List<InvoiceNumberResponse> invoiceNumber;

    @JsonProperty("itens")
    private List<PurchaseItemResponse> items;
}
