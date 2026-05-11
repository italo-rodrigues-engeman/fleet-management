package com.indux.modules.modulo_mega.application.dto.abc;

import java.math.BigDecimal;

public record AbcItemBaseRow(
    Integer groupCode,
    String groupName,
    Integer itemCode,
    String itemName,
    String type,
    BigDecimal totalValue,
    BigDecimal totalQty,
    BigDecimal averagePrice,
    String status
) {}
