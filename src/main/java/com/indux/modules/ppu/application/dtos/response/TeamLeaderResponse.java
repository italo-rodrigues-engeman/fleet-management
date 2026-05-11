package com.indux.modules.ppu.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamLeaderResponse {
    @JsonProperty("turno")
    private String shift;

    @JsonProperty("quantidade")
    private Integer quantity;

    @JsonProperty("valor")
    private Double value;
}

