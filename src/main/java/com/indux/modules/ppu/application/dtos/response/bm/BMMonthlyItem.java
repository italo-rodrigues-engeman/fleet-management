package com.indux.modules.ppu.application.dtos.response.bm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BMMonthlyItem {
    @JsonProperty("id")
    String id;
    
    @JsonProperty("numero")
    String number;
    
    @JsonProperty("nome")
    String name;
    
    @JsonProperty("total")
    BigDecimal total;
    
    @JsonProperty("quantidade")
    Number quantity;
}