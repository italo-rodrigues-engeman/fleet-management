package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.poi.util.Units;

import java.util.List;

public record RangeReturn(
        @JsonProperty("nome")
        String name,
        @JsonProperty("quantidade")
        Integer amount,
        @JsonProperty("medida")
        Measures measure,
        @JsonProperty("unidade")
        UnitMeasures unit,
        @JsonProperty("faixa1")
        Double range1,
        @JsonProperty("faixa2")
        Double range2,
        @JsonProperty("resolucao")
        List<Double> resolution,
        @JsonProperty("referencia")
        List<Double> reference,
        @JsonProperty("tolerancia")
        List<ToleranceReturn> tolerance
) {
}