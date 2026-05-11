package com.indux.modules.modulo_mega.persistence.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "purchase_mega")
public class PurchaseProcessDocument {
    @Id
    private String id;
    private String ref;
    private List<RequestDocument> requests;
    private List<PurchaseOrderDocument> order;
    private List<Integer> ap;
    private List<InvoiceNumberDocument> invoiceNumber;
    private List<PurchaseItemDocument> items;
}
