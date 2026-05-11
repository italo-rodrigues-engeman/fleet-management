package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PurchaseOrderResponse {

    @JsonProperty("numero_pedido")
    private Integer orderNumber;

    @JsonProperty("data_pedido")
    private Instant orderDate;

    @JsonProperty("comprador")
    private String buyer;
}
