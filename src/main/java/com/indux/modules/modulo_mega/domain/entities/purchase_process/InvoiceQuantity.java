package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceQuantity {
    private Long invoiceNumber;
    private Integer ap;
    private Integer quantity;
    private Integer unitValue;
}
