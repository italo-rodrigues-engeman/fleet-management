package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrder {
    private Integer orderNumber;
    private Instant orderDate;
    private String buyer;
}
