package com.indux.modules.modulo_mega.domain.model;

import lombok.Builder;
import lombok.Value; 
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class MegaEntityOrder {
    
    
    private Integer id;

    
    private Integer idItem;
    private String  noteNumber;
   
    private String itemName;

   
    private String unitOfMeasure;

    private Integer orderNumber;

    private String orderType;

    private Integer supplierCode;

    private String supplierName;

    private String cnpj;

    private Integer groupCode;

    private String groupName;

    private String itemType; // Service or Product

    private LocalDate orderDate;

    private LocalDate approvalDate;

    private Long orderCount;

    private LocalDate deliveryDate;

    private String orderStatus;

    private String itemStatus;

    private BigDecimal qtdItensTotal;

    private BigDecimal averagePriceFromSupplier;

    private BigDecimal totalItemValue;

    private String buyerName;

    private Integer branchId;

    private String branchName;

    private Integer projectCode;

    private String projectName;

    private LocalDate creationDate;

    private String registerUser;

    private String registerUserEdition;
}