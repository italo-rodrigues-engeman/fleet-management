package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceNumber {
    private Long invoiceNumber;
    private Instant invoiceDate;
}
