package com.indux.modules.ppu.application.dtos.response.bm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BMPlatformReport {
    @JsonProperty("plataforma")
    String platform;

    @JsonProperty("quantidade")
    Double totalQuantity;

    @JsonProperty("total")
    BigDecimal totalValue = BigDecimal.ZERO;

    @JsonProperty("tipo")
    String type;
}