package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MeasurementPeriod {
    @JsonProperty("diaAbertura") Integer openingDay;
    @JsonProperty("diaFechamento") Integer closingDay;
}
