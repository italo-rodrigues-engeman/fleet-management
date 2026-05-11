package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    @JsonProperty("quantidade")
    public Integer quantity;

    @JsonProperty("data_de_aprovacao")
    public Instant orderApprovalDate;
}
