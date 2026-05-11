package com.indux.modules.modulo_mega.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AbcItemDTO {
    
    private Integer groupCode;
    private String  groupName;
    private Integer idItem;
    private String  itemName;
    private String itemType;
    private BigDecimal qtdItensTotal;
    private BigDecimal totalItemValue;
    private Long orderCount;   
    private String itemStatus;
    
    private BigDecimal averagePriceFromSupplier;
    private String situation;
    private String orderType;
    
    
    
   

    // --- Campos do Join com MegaEntityOrganogram ---
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