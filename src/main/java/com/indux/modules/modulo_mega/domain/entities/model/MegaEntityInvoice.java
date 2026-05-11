package com.indux.modules.modulo_mega.domain.model;

import lombok.Builder;
import lombok.Value; 
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class MegaEntityInvoice {
    
    
   
    private String id;

    private Integer idItem;

    private String itemName;

    private String unitOfMeasure;

    private Integer orderNumber;

    private String noteNumber;

    private String approve;

    private String docType;

    private Integer supplierCode;

    private String supplierName;

    private String cnpj;

    private Integer groupCode;

    private String groupName;

    private String invoiceType;
    
    private String invoiceDate;

    private BigDecimal averagePriceFromSupplier;

    private BigDecimal totalItemValue;

    private BigDecimal qtdItensTotal;

    private Integer branchId;

    private String branchName;

    private Integer projectCode;

    private String projectName;

    private String projectValue;

    private LocalDate creationDate;

    private String registerUser;

    private String registerUserEdition;

}