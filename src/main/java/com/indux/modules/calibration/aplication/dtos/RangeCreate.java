package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RangeCreate {
    @JsonProperty("nome")
    private String name;
    @JsonProperty("quantidade")
    private Integer amount;
    @JsonProperty("measureId")
    private String measureId;
    @JsonProperty("unidadeId")
    private String unitId;
    @JsonProperty("faixa1")
    private Double range1;
    @JsonProperty("faixa2")
    private Double range2;
    @JsonProperty("resolucao")
    private List<Double> resolution;
    @JsonProperty("referencia")
    private List<Double> reference;
    @JsonProperty("tolerancia")
    private List<ToleranceCreate> tolerance;
}