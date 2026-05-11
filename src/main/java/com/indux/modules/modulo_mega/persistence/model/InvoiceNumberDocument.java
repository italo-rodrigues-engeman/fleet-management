package com.indux.modules.modulo_mega.persistence.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceNumberDocument {
    private Long invoiceNumber;
    private Instant invoiceDate;

}
