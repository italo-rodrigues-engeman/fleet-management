package com.indux.modules.modulo_mega.domain.model;

import lombok.Value;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class MegaEntityItens {
    Integer idItem;
    String itemName;
    Integer groupCode;
    String groupName;
    BigDecimal totalQuantity;
    BigDecimal totalValue;
    BigDecimal averagePrice;
    Long orderCount;
    String status;
    LocalDate creationDate;
    String orderStatus;
    String unitOfMeasure;
    String itemType;
    Integer projectCode;
    
}