package com.indux.modules.modulo_mega.persistence.gateway;

import com.indux.modules.modulo_mega.domain.entity.abc.AbcFilter;
import com.indux.modules.modulo_mega.domain.entity.abc.AbcItem;
import com.indux.modules.modulo_mega.domain.repository.AbcItemGateway;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
@RequiredArgsConstructor
public class AbcItemGatewayImpl implements AbcItemGateway {

    private static final String COLLECTION = "purchase_mega";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<AbcItem> findItemsForAbcAnalysis(AbcFilter filter) {
        Aggregation aggregation = Aggregation.newAggregation(
                buildPipeline(filter)
        );

        return mongoTemplate
                .aggregate(aggregation, COLLECTION, AbcItem.class)
                .getMappedResults();
    }

    private List<AggregationOperation> buildPipeline(AbcFilter filter) {
        List<AggregationOperation> operations = new ArrayList<>();

        operations.add(Aggregation.unwind("items", true));
        operations.add(addCalculatedFields());
        operations.add(Aggregation.match(buildMatchCriteria(filter)));
        operations.add(groupItems());
        operations.add(projectResult());
        operations.add(Aggregation.sort(Sort.Direction.DESC, "totalItemValue"));

        return operations;
    }

    private AggregationOperation addCalculatedFields() {
        return context-> Document.parse("""
            {
              "$addFields": {
                "effectiveDate": {
                  "$ifNull": [
                    { "$arrayElemAt": ["$order.orderDate", 0] },
                    {
                      "$ifNull": [
                        { "$arrayElemAt": ["$invoiceNumber.invoiceDate", 0] },
                        {
                          "$ifNull": [
                            "$items.order.orderApprovalDate",
                            { "$arrayElemAt": ["$requests.requestDate", 0] }
                          ]
                        }
                      ]
                    }
                  ]
                },
                "calculatedOrderType": {
                  "$switch": {
                    "branches": [
                      {
                        "case": {
                          "$and": [
                            {
                              "$or": [
                                { "$gt": [{ "$size": { "$ifNull": ["$order", []] } }, 0] },
                                { "$ne": ["$items.order", null] }
                              ]
                            },
                            {
                              "$gt": [{ "$size": { "$ifNull": ["$requests", []] } }, 0]
                            }
                          ]
                        },
                        "then": "Padrão"
                      },
                      {
                        "case": {
                          "$and": [
                            {
                              "$or": [
                                { "$gt": [{ "$size": { "$ifNull": ["$order", []] } }, 0] },
                                { "$ne": ["$items.order", null] }
                              ]
                            },
                            {
                              "$not": {
                                "$gt": [{ "$size": { "$ifNull": ["$requests", []] } }, 0]
                              }
                            }
                          ]
                        },
                        "then": "Livre"
                      },
                      {
                        "case": {
                          "$and": [
                            {
                              "$not": {
                                "$or": [
                                  { "$gt": [{ "$size": { "$ifNull": ["$order", []] } }, 0] },
                                  { "$ne": ["$items.order", null] }
                                ]
                              }
                            },
                            {
                              "$not": {
                                "$gt": [{ "$size": { "$ifNull": ["$requests", []] } }, 0]
                              }
                            }
                          ]
                        },
                        "then": "Lançamento"
                      }
                    ],
                    "default": "Indefinido"
                  }
                },
                "calculatedSituation": {
                  "$cond": {
                    "if": {
                      "$and": [
                        {
                          "$not": {
                            "$or": [
                              { "$gt": [{ "$size": { "$ifNull": ["$invoiceNumber", []] } }, 0] },
                              { "$gt": [{ "$size": { "$ifNull": ["$items.invoices", []] } }, 0] }
                            ]
                          }
                        },
                        {
                          "$in": [
                            "$items.orderStatus",
                            ["Pedido Atendido", "Pedido Encerrado"]
                          ]
                        }
                      ]
                    },
                    "then": "Baixado",
                    "else": "Regular"
                  }
                },
                "safeQuantity": {
                  "$ifNull": ["$items.order.quantity", 0]
                },
                "totalLineValue": {
                  "$multiply": [
                    { "$ifNull": ["$items.value", 0] },
                    { "$ifNull": ["$items.order.quantity", 0] }
                  ]
                }
              }
            }
        """);
    }

    private Criteria buildMatchCriteria(AbcFilter filter) {
        Criteria criteria = new Criteria();

        LocalDate startDate = filter.getStartDate() != null
                ? filter.getStartDate()
                : LocalDate.now().minusYears(1);

        LocalDate endDate = filter.getEndDate() != null
                ? filter.getEndDate()
                : LocalDate.now();

        criteria.and("effectiveDate")
                .gte(startDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                .lte(endDate.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC));

        if (isNotEmpty(filter.getIdItem())) {
            criteria.and("items.idItem").in(filter.getIdItem());
        }

        if (isNotEmpty(filter.getStatus())) {
            criteria.and("items.orderStatus").in(filter.getStatus());
        }

        if (isNotEmpty(filter.getCategory())) {
            criteria.orOperator(
                    Criteria.where("items.type").in(filter.getCategory()),
                    Criteria.where("items.category").in(filter.getCategory())
            );
        }

        if (isNotEmpty(filter.getProjectCode())) {
            criteria.and("items.projectId").in(filter.getProjectCode());
        }

        if (isNotEmpty(filter.getContractCode())) {
            criteria.and("items.contractId").in(filter.getContractCode());
        }

        if (isNotEmpty(filter.getRegionalCode())) {
            criteria.and("items.regionalId").in(filter.getRegionalCode());
        }

        if (isNotEmpty(filter.getSituation())) {
            criteria.and("calculatedSituation").in(filter.getSituation());
        }

        List<String> mappedOrderTypes = mapOrderTypes(filter.getOrderType());
        if (!mappedOrderTypes.isEmpty()) {
            criteria.and("calculatedOrderType").in(mappedOrderTypes);
        }

        return criteria;
    }

    private AggregationOperation groupItems() {
        return Aggregation.group(
                        "items.idItem",
                        "items.groupCode",
                        "items.groupName",
                        "items.description",
                        "items.type",
                        "items.orderStatus",
                        "calculatedOrderType",
                        "calculatedSituation"
                )
                .first("items.groupCode").as("groupCode")
                .first("items.groupName").as("groupName")
                .first("items.idItem").as("idItem")
                .first("items.description").as("itemName")
                .first("items.type").as("itemType")
                .first("items.orderStatus").as("itemStatus")
                .first("calculatedSituation").as("situation")
                .first("calculatedOrderType").as("orderType")
                .sum("safeQuantity").as("qtdItensTotal")
                .sum("totalLineValue").as("totalItemValue")
                .count().as("orderCount")
                .first("items.projectId").as("projectCode")
                .first("items.contractId").as("contractCode")
                .first("items.contractName").as("contractName")
                .first("items.regionalId").as("regionalCode")
                .first("items.regionalName").as("regionalName");
    }

    private AggregationOperation projectResult() {
        return context -> Document.parse("""
            {
              "$project": {
                "_id": 0,
                "groupCode": 1,
                "groupName": 1,
                "idItem": 1,
                "itemName": 1,
                "itemType": 1,
                "itemStatus": 1,
                "situation": 1,
                "orderType": 1,
                "qtdItensTotal": 1,
                "totalItemValue": 1,
                "orderCount": 1,
                "projectCode": 1,
                "contractCode": 1,
                "contractName": 1,
                "regionalCode": 1,
                "regionalName": 1,
                "averagePriceFromSupplier": {
                  "$cond": [
                    { "$eq": ["$qtdItensTotal", 0] },
                    0,
                    { "$divide": ["$totalItemValue", "$qtdItensTotal"] }
                  ]
                }
              }
            }
        """);
    }

    private List<String> mapOrderTypes(List<String> orderTypes) {
        List<String> mapped = new ArrayList<>();

        if (!isNotEmpty(orderTypes)) {
            return mapped;
        }

        for (String value : orderTypes) {
            if (value == null || value.isBlank()) {
                continue;
            }

            String normalized = normalize(value);

            if (normalized.contains("PADRAO")) {
                mapped.add("Padrão");
            } else if (normalized.contains("PEDIDO") && normalized.contains("LIVRE")) {
                mapped.add("Livre");
            } else if (normalized.contains("LANCAMENTO")) {
                mapped.add("Lançamento");
            } else if (normalized.contains("INDEFINIDO")) {
                mapped.add("Indefinido");
            }
        }

        return mapped;
    }

    private String normalize(String value) {
        return value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("Ã", "A")
                .replace("Á", "A")
                .replace("À", "A")
                .replace("Â", "A")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ô", "O")
                .replace("Õ", "O")
                .replace("Ú", "U")
                .replace("Ç", "C");
    }

    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}