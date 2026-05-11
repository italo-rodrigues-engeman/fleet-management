package com.indux.modules.modulo_mega.persistence.model;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceQuantityDocument {
    private Long invoiceNumber;
    private Integer ap;
    private Integer quantity;
    private Integer unitValue;

}
