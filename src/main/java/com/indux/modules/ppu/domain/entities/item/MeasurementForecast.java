package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder @AllArgsConstructor @NoArgsConstructor @Getter @Setter
public class MeasurementForecast {
    private String id;
    @JsonProperty("plataforma")
    private String platform;
    @JsonProperty("total")
    private Integer total;
}
