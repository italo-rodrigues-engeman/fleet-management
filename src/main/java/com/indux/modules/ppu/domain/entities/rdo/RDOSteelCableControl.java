package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RDOSteelCableControl {
    @JsonProperty("operacional")
    private boolean operational;
    @JsonProperty("responsabilidadeInoperancia")
    private String inoperabilityResponsibility;
    @JsonProperty("disponibilidadeReserva")
    private boolean reserveAvailable;
    @JsonProperty("justificativa")
    private String justification;
}