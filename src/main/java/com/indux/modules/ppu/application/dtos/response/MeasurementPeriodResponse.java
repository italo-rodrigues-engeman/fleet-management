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
public class MeasurementPeriodResponse {
    @JsonProperty("diaAbertura")
    private Integer openingDay;

    @JsonProperty("diaFechamento")
    private Integer closingDay;
}

