package com.indux.modules.modulo_mega.domain.entity.abc;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbcItem {
    private Integer groupCode;
    private String groupName;
    private Integer idItem;
    private String itemName;
    private String itemType;
    private BigDecimal qtdItensTotal;
    private BigDecimal totalItemValue;
    private Long orderCount;
    private String itemStatus;
    private BigDecimal averagePriceFromSupplier;
    private String situation;
    private String orderType;

    // Hierarchy
    private Integer projectCode;
    private Integer contractCode;
    private String contractName;
    private Integer sectorId;
    private String sectorName;
    private String acronymSector;
    private Integer regionalCode;
    private String regionalName;
    private String acronymRegional;
    private Integer superId;
    private String superName;
    private String acronymSuper;
    private Integer directoryId;
    private String directoryName;
    private String acronymDirectory;
}
