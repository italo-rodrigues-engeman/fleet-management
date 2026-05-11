package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RDOSteelCable {
    @JsonProperty("id")
    private String steelCableId;
    @JsonProperty("quantidade")
    private int quantity;
    @JsonProperty("horasExtras")
    private String overtime;
    @JsonProperty("disposicao")
    private String disposition;
}