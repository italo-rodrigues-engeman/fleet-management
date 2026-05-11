package com.indux.modules.modulo_mega.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbcCurveGroupDTO {

    @JsonProperty("classe_curva")
    private String curveClass; // "A", "B", ou "C"

    @JsonProperty("itens")
    private List<AbcItemDetailDTO> items;

    @JsonProperty("valor_total_grupo")
    private BigDecimal totalGroupValue;

    @JsonProperty("volume_total_grupo")
    private BigDecimal totalGroupVolume;
}

