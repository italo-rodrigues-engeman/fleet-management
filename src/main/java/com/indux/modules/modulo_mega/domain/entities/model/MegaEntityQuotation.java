package com.indux.modules.modulo_mega.domain.model;

import lombok.Builder;
import lombok.Value; 
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class MegaEntityQuotation {
    
    
    private String id;

    private Integer itemCode;

    private String itemName;

    private String unitOfMeasure;

    private Integer quotationNumber;

    private Integer orderNumber;

    private LocalDate quotationDate;

    private LocalDate quotationDateApprove;

    private String quotationDescription;

    private String quotationStatus;

    private Integer groupCode;

    private String groupName;

    private String quotationType;

    private Integer supplierCode;

    private String supplierName;

    private String cnpj;

    private BigDecimal quotationQuantity;

    private BigDecimal unitValue;

    private BigDecimal totalValue;

    private BigDecimal costOfLastPurchase;

    private String orderGenerated;

    private String quotationUserName;

    private Integer branchId;

    private String branchName;
}