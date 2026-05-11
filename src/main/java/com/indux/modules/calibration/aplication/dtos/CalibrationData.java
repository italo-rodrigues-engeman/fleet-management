package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationData {
    @JsonProperty("medido")
    private Double measured;
    
    @JsonProperty("incerteza")
    private Double uncertainty;
    
    @JsonProperty("maxTolerancia")
    private Double maxTolerance;
    
    @JsonProperty("erro")
    private Double erro;

    @JsonProperty("usado")
    private Boolean use;
}