package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TeamLeader {
    @JsonProperty("turno")
    private String shift;
    @JsonProperty("quantidade")
    private Integer quantity;
    @JsonProperty("valor")
    private Double value;
}
