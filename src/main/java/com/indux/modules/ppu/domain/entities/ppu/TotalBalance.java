package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class TotalBalance {
    private String id;
    @JsonProperty("numeroPPU") private String ppuNumber;
    @JsonProperty("numero") private String genericNumber;
    @JsonProperty("nome") private String name;
    @JsonProperty("quantidadeTotal") private Long totalQuantity;
    @JsonProperty("valorTotal") private BigDecimal totalValue;

}
