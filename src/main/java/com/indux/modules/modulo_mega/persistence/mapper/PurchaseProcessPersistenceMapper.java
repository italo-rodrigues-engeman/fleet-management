package com.indux.modules.modulo_mega.persistence.mapper;

import com.indux.modules.modulo_mega.domain.entities.purchase_process.*;
import com.indux.modules.modulo_mega.persistence.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseProcessPersistenceMapper {

    PurchaseProcessDocument fromModel(PurchaseProcess model);
    PurchaseProcess toEntity(PurchaseProcessDocument entity);
    List<PurchaseProcessDocument> fromPurchaseProcessList(List<PurchaseProcess> models);
    List<PurchaseProcess> toPurchaseProcessList(List<PurchaseProcessDocument> entities);

    InvoiceNumberDocument fromModel(InvoiceNumber model);
    InvoiceNumber toEntity(InvoiceNumberDocument entity);
    List<InvoiceNumberDocument> fromInvoiceNumberList(List<InvoiceNumber> models);
    List<InvoiceNumber> toInvoiceNumberList(List<InvoiceNumberDocument> entities);

    InvoiceQuantityDocument fromModel(InvoiceQuantity model);
    InvoiceQuantity toEntity(InvoiceQuantityDocument entity);
    List<InvoiceQuantityDocument> fromInvoiceQuantityList(List<InvoiceQuantity> models);
    List<InvoiceQuantity> toInvoiceQuantityList(List<InvoiceQuantityDocument> entities);

    PurchaseItemDocument fromModel(PurchaseItem model);
    PurchaseItem toEntity(PurchaseItemDocument entity);
    List<PurchaseItemDocument> fromPurchaseItemList(List<PurchaseItem> models);
    List<PurchaseItem> toPurchaseItemList(List<PurchaseItemDocument> entities);

    PurchaseOrderDocument fromModel(PurchaseOrder model);
    PurchaseOrder toEntity(PurchaseOrderDocument entity);
    List<PurchaseOrderDocument> fromPurchaseOrderList(List<PurchaseOrder> models);
    List<PurchaseOrder> toPurchaseOrderList(List<PurchaseOrderDocument> entities);

    OrderItemDocument fromEntity(OrderItem entity);
    OrderItem toEntity(OrderItemDocument model);
    List<OrderItemDocument> fromEntityList(List<OrderItem> entities);
    List<OrderItem> toEntityList(List<OrderItemDocument> models);

    RequestDocument fromModel(Request model);
    Request toEntity(RequestDocument entity);
    List<RequestDocument> fromRequestList(List<Request> models);
    List<Request> toRequestList(List<RequestDocument> entities);

    SupplierDocument fromModel(Supplier model);
    Supplier toEntity(SupplierDocument entity);
    List<SupplierDocument> fromSupplierList(List<Supplier> models);
    List<Supplier> toSupplierList(List<SupplierDocument> entities);

}
