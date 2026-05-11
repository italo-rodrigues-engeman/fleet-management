package com.indux.modules.modulo_mega.persistence.repository;

import com.indux.modules.modulo_mega.application.dto.OrdersFilter;
import com.indux.modules.modulo_mega.persistence.model.PurchaseProcessDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class PurchaseProcessRepositoryImpl implements PurchaseProcessFilterRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<PurchaseProcessDocument> filter(Pageable pageable, OrdersFilter filter) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        addIn(criteriaList, "order.orderNumber", filter.getNumeroPedido());
        addIn(criteriaList, "items.idItem", filter.getCodigoItem());
        addIn(criteriaList, "items.groupCode", filter.getCodigoGrupo());
        addIn(criteriaList, "items.supplier.supplierCod", filter.getCodigoFornecedor());
        addIn(criteriaList, "items.orderType", filter.getTipoPedido());
        addIn(criteriaList, "order.buyer", filter.getComprador());
        addIn(criteriaList, "requests.requester", filter.getSolicitante());
        addIn(criteriaList, "items.orderStatus", filter.getStatusItem());
        addIn(criteriaList, "items.typePurchase", filter.getTipoPedidoCalculado());
        addIn(criteriaList, "items.category", filter.getSituacao());

        // organograma
        //todo: falta ainda diretoria, talvez fazer no service facilite
        addIn(criteriaList, "items.regionalId", filter.getRegional());
        addIn(criteriaList, "items.projectId", filter.getProjeto());
        addIn(criteriaList, "items.branchCode", filter.getFilial());
        addIn(criteriaList, "items.contractId", filter.getContrato());


        addRange(criteriaList, "items.value", filter.getPrecoMedMin(), filter.getPrecoMedMax());
        addQuantityRange(criteriaList, filter.getQtdeComprasMin(), filter.getQtdeComprasMax());

        addIn(criteriaList, "order.orderNumber", filter.getOrderNumbersFilter());
        addIn(criteriaList, "items.unitMeasurement", filter.getUnidadeMedida());

        if (filter.getSolicitacao() != null) {
            criteriaList.add(Criteria.where("requests.requestNumber").is(filter.getSolicitacao()));
        }

        addIn(criteriaList, "ap", parseIntegerList(filter.getAp()));
        addIn(criteriaList, "invoiceNumber.invoiceNumber", filter.getDocumento());
        addIn(criteriaList, "items.type", filter.getCategoria());
        addIn(criteriaList, "items.orderStatus", filter.getStatusPedido());

        addDateRange(criteriaList, "requests.requestDate", filter.getPedidoStartDate(), filter.getPedidoEndDate());
        addDateRange(criteriaList, "invoiceNumber.invoiceDate", filter.getEmissaoNfStartDate(), filter.getEmissaoNfEndDate());
        addDateRange(criteriaList, "items.order.orderApprovalDate", filter.getAprovacaoPedidoCompraStartDate(), filter.getAprovacaoPedidoCompraEndDate());
        addDateRange(criteriaList, "items.request.requestApprovalDate", filter.getAprovacaoSolicitacaoStartDate(), filter.getAprovacaoSolicitacaoEndDate());
        addDateRange(criteriaList, "items.quotation.quotationApprovalDate", filter.getAprovacaoMapaStartDate(), filter.getAprovacaoMapaEndDate());

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, PurchaseProcessDocument.class);

        query.with(pageable);

        List<PurchaseProcessDocument> content = mongoTemplate.find(query, PurchaseProcessDocument.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<String> getStatus() {
            return mongoTemplate.query(PurchaseProcessDocument.class)
                    .distinct("items.orderStatus")
                    .as(String.class)
                    .all()
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();

    }

    private void addIn(List<Criteria> criteriaList, String field, List<?> values) {
        if (values != null && !values.isEmpty()) {
            criteriaList.add(Criteria.where(field).in(values));
        }
    }

    private void addQuantityRange(List<Criteria> criteriaList, BigDecimal min, BigDecimal max) {
        if (min == null && max == null) {
            return;
        }

        Criteria invoiceQty = Criteria.where("invoices.quantity");
        if (min != null) {
            invoiceQty = invoiceQty.gte(min.doubleValue());
        }
        if (max != null) {
            invoiceQty = invoiceQty.lte(max.doubleValue());
        }

        Criteria orderQty = Criteria.where("order.quantity");
        if (min != null) {
            orderQty = orderQty.gte(min.doubleValue());
        }
        if (max != null) {
            orderQty = orderQty.lte(max.doubleValue());
        }

        Criteria invoiceCase = new Criteria().andOperator(
                Criteria.where("invoices").exists(true),
                Criteria.where("invoices").ne(List.of()),
                invoiceQty
        );

        Criteria orderCase = new Criteria().andOperator(
                new Criteria().orOperator(
                        Criteria.where("invoices").exists(false),
                        Criteria.where("invoices").is(null),
                        Criteria.where("invoices").size(0)
                ),
                orderQty
        );

        criteriaList.add(
                Criteria.where("items").elemMatch(
                        new Criteria().orOperator(invoiceCase, orderCase)
                )
        );
    }
    private void addRange(List<Criteria> criteriaList, String field, BigDecimal min, BigDecimal max) {
        if (min == null && max == null) {
            return;
        }

        Criteria criteria = Criteria.where(field);

        if (min != null) {
            criteria = criteria.gte(min.doubleValue());
        }

        if (max != null) {
            criteria = criteria.lte(max.doubleValue());
        }

        criteriaList.add(criteria);
    }

    private void addDateRange(List<Criteria> criteriaList, String field, LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return;
        }

        Criteria criteria = Criteria.where(field);

        if (start != null) {
            criteria = criteria.gte(start.atStartOfDay());
        }

        if (end != null) {
            criteria = criteria.lte(end.atTime(23, 59, 59, 999_999_999));
        }

        criteriaList.add(criteria);
    }

    private List<Integer> parseIntegerList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::valueOf)
                .toList();
    }
}