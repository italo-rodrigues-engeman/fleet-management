package com.indux.modules.ppu.application.dtos.response.bm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BMTimeline {
    String id;
    @JsonProperty("numero")
    String number;
    @JsonProperty("nome")
    String name;
    @JsonProperty("unidade")
    String unit;
    @JsonIgnore Boolean isOvertimeService;
    @JsonIgnore String overtimeService;
    @JsonIgnore List<String> children;
    @JsonProperty("previstoDiario")
    Integer qtdExpected;
    @JsonProperty("valorUnitario")
    BigDecimal unitValue;
    @JsonProperty("plataforma")
    String platform;
    @JsonProperty("linhaTempo")
    private List<DailyEntry> timeline = new ArrayList<>();
    @JsonProperty("servicoPai")
    private String parentId;

    public record DailyEntry(
            @JsonProperty("data") LocalDate date,
            @JsonProperty("dia") Integer day,
            @JsonProperty("quantidade") Number quantity
    ) { }

}
