package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    public Integer quantity;
    public Instant orderApprovalDate;
}

