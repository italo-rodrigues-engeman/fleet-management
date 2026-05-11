package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseProcess {
    private String id;
    private String ref;
    private List<Request> requests;
    private List<PurchaseOrder> order;
    private List<Integer> ap;
    private List<InvoiceNumber> invoiceNumber;
    private List<PurchaseItem> items;
}
