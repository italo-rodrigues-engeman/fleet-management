package com.indux.modules.ppu.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalBalanceResponse {
    private String id;

    @JsonProperty("numeroPPU")
    private String ppuNumber;

    @JsonProperty("numero")
    private String genericNumber;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("quantidadeTotal")
    private Long totalQuantity;

    @JsonProperty("valorTotal")
    private BigDecimal totalValue;
}

