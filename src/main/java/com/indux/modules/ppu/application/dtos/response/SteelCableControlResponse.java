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
public class SteelCableControlResponse {
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
}

