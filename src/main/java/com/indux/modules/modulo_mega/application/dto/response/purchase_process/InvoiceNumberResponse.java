package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InvoiceNumberResponse {

    @JsonProperty("numero_nota_fiscal")
    private Long invoiceNumber;

    @JsonProperty("data_nota_fiscal")
    private Instant invoiceDate;
}
