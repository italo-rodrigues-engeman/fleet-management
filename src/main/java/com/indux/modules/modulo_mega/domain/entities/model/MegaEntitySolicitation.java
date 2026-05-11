package com.indux.modules.modulo_mega.domain.model;

import lombok.Builder;
import lombok.Value; 
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class MegaEntitySolicitation {
    
    
   
    
    private String id;

    private Integer idItem;

    private String itemName;

    private String unitOfMeasure;

    private Integer orderNumber;

    private Integer solicitation;

    private LocalDate solDate;

    private LocalDate approvalDate;

    private Integer groupCode;

    private String groupName;

    private String itemType; // Serviço ou Produto

    private String itemStatus;

    private BigDecimal requestedQuantity;

    private String requesterName;

    private Integer branchId;

    private String branchName;

    private Integer projectCode;

    private String projectName;

    private LocalDate creationDate;

    private String registerUser;

    private String registerUserEdition;

    private Integer quotationNumber;

}