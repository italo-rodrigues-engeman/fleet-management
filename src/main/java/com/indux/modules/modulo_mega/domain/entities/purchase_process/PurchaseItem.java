package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseItem {
    private Integer idItem;
    private String description;
    private String unitMeasurement;
    private Integer groupCode;
    private String groupName;
    private String type;
    private BigDecimal value;
    private Supplier supplier;
    private String orderType;
    private String orderStatus;
    private String typePurchase;
    private String category;
    private Instant orderApprovalDate;
    private OrderItem order;
    private List<InvoiceQuantity> invoices;
    private Integer projectId;
    private String projectName;
    private Integer branchCode;
    private String branchName;
    private Integer regionalId;
    private String regionalName;
    private Integer contractId;
    private String contractName;
}
