package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.application.dto.response.purchase_process.*;
import com.indux.modules.modulo_mega.domain.entities.purchase_process.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseProcessApplicationMapper {

    PurchaseProcessResponse toResponse(PurchaseProcess domain);
    List<PurchaseProcessResponse> toPurchaseProcessResponseList(List<PurchaseProcess> domains);

    InvoiceNumberResponse toResponse(InvoiceNumber domain);
    List<InvoiceNumberResponse> toInvoiceNumberResponseList(List<InvoiceNumber> domains);

    InvoiceQuantityResponse toResponse(InvoiceQuantity domain);
    List<InvoiceQuantityResponse> toInvoiceQuantityResponseList(List<InvoiceQuantity> domains);

    PurchaseItemResponse toResponse(PurchaseItem domain);
    List<PurchaseItemResponse> toPurchaseItemResponseList(List<PurchaseItem> domains);

    PurchaseOrderResponse toResponse(PurchaseOrder domain);
    List<PurchaseOrderResponse> toPurchaseOrderResponseList(List<PurchaseOrder> domains);

    RequestResponse toResponse(Request domain);
    List<RequestResponse> toRequestResponseList(List<Request> domains);

    OrderItemResponse toResponse(OrderItem domain);
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> domains);

    SupplierResponse toResponse(Supplier domain);
    List<SupplierResponse> toSupplierResponseList(List<Supplier> domains);

}
