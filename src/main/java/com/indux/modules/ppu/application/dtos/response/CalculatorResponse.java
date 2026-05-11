package com.indux.modules.ppu.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CalculatorResponse {
    @JsonProperty("adicionalNoturno") private Duration nightShiftPremium;
    @JsonProperty("horasNormais") private Duration normalHours;
    @JsonProperty("horasTotais") private Duration hourTotais;
    @JsonProperty("horasExtrasTotais") private Duration overtimeHourTotais;
}
