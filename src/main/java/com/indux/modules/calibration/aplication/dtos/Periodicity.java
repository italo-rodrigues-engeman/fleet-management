package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Periodicity {
    @JsonProperty("ambiente") private String environment;
    @JsonProperty("frequencia") private String frequency;
    @JsonProperty("classe") private String classe;
    @JsonProperty("meses") private Integer months;
}
