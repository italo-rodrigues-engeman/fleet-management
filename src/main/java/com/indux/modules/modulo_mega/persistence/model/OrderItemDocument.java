package com.indux.modules.modulo_mega.persistence.model;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDocument {
    public Integer quantity;
    public Instant orderApprovalDate;
}
