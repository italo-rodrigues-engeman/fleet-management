package com.indux.modules.ppu.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CraneControlResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("localizacao")
    private String location;

    @JsonProperty("ultimaLubrificacao")
    private LocalDateTime lastLubrication;

    @JsonProperty("operacional")
    private Boolean operational;

    @JsonProperty("horimetro")
    private String hourMeter;

    @JsonProperty("checklist")
    private Boolean checklist;

    @JsonProperty("colaboradorChecklist")
    private List<SimpleEmployeeDTO> colaboradorChecklist;

    @JsonProperty("inoperancia")
    private String inoperabilityResponsibility;

    @JsonProperty("justificativaInoperancia")
    private String justification;

    @JsonProperty("plataforma")
    private String platform;

    @JsonProperty("sistemas")
    private List<String> systems;
}

