package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DataLog {
    @JsonProperty("nome") private String name;
    @JsonProperty("data") private LocalDateTime date;
    @JsonProperty("acao") private String action;
}

