package com.indux.modules.modulo_mega.persistence.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderDocument {
    private Integer orderNumber;
    private Instant orderDate;
    private String buyer;

}
