package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RDOAccessoryKit {
    @JsonProperty("id")
    private String accessoryKitId;
    @JsonProperty("quantidade")
    private int quantity;
    @JsonProperty("completo")
    private boolean complete;
    @JsonProperty("observacao")
    private String observation;
}