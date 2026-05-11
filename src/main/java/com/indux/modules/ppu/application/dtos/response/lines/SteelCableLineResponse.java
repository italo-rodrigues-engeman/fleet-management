package com.indux.modules.ppu.application.dtos.response.lines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SteelCableLineResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("numero")
    private String genericNumber;

    @JsonProperty("numeroPPU")
    private String ppuNumber;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("unidadeMedida")
    private String unitOfMeasurement;

    @JsonProperty("valor")
    private Double value;

    @JsonProperty("fator")
    private Double factor;

    @JsonProperty("linhaAuditavel")
    private String auditableLine;

    @JsonProperty("linhasAuditaveis")
    private List<AuditableLineConfig> auditableLines;

    @JsonProperty("certificateUrl")
    private String certificate;

    @JsonProperty("totalPrevisto")
    private Integer totalPlanned;

    @JsonProperty("plataformas")
    private List<String> platforms;
}

