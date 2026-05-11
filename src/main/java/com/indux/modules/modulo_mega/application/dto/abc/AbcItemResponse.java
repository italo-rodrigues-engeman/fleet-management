package com.indux.modules.modulo_mega.application.dto.abc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AbcItemResponse(
    @JsonProperty("numero") Integer number,
    @JsonProperty("cod_grupo") Integer groupCode,
    @JsonProperty("grupo") String groupName,
    @JsonProperty("cod_item") Integer itemCode,
    @JsonProperty("item") String itemName,
    @JsonProperty("tipo") String type,
    @JsonProperty("vlr_total_rs") BigDecimal totalValue,
    @JsonProperty("qtde") BigDecimal totalQty,
    @JsonProperty("preco_medio") BigDecimal averagePrice,
    @JsonProperty("status") String status,
    @JsonProperty("percentual") BigDecimal percentage,
    @JsonProperty("percentual_acumulado") BigDecimal accumulatedPercentage,
    @JsonProperty("curva") String curve
) {}
