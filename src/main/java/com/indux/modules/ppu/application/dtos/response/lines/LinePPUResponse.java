package com.indux.modules.ppu.application.dtos.response.lines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinePPUResponse {
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

    @JsonProperty("totalPrevisto")
    private List<MeasurementForecast> measurementForecasts;

    @JsonProperty("plataformas")
    private List<String> platforms;
}
