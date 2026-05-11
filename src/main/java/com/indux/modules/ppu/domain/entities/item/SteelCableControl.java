package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.item.SteelCableControlDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa o controle do sistema de cabo de aço.
 * Armazenado dentro da PPU.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SteelCableControl {
    @JsonProperty("localizacao")
    private String location;
    @JsonProperty("sistema")
    private String system;
    @JsonProperty("operacional")
    private Boolean operational;
    @JsonProperty("responsavelInoperancia")
    private String inoperabilityResponsibility;
    @JsonProperty("caboReserva")
    private Boolean hasSpareCable;
    @JsonProperty("justificativa")
    private String justification;
    @JsonProperty("plataforma")
    private String platform;

    public static SteelCableControl fromDTO(SteelCableControlDTO dto) {
        return new SteelCableControl(
                dto.localizacao(),
                dto.sistema(),
                dto.operacional(),
                dto.responsabilidadeInoperancia(),
                dto.caboReserva(),
                dto.justificativaOperacional(),
                dto.plataforma()
        );
    }
}
