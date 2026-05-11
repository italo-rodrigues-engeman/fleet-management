package com.indux.modules.modulo_mega.persistence.repository.abc;

import com.indux.modules.modulo_mega.application.dto.abc.AbcItemBaseRow;
import com.indux.modules.modulo_mega.application.dto.abc.AbcItemFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@RequiredArgsConstructor
public class PurchaseMegaAbcRepositoryImpl implements PurchaseMegaAbcRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<AbcItemBaseRow> findAbcItems(AbcItemFilter filter) {
        List<AggregationOperation> operations = new ArrayList<>();

        if (filter.getDataInicial() != null && filter.getDataFinal() != null) {
            LocalDateTime start = filter.getDataInicial().atStartOfDay();
            LocalDateTime end = filter.getDataFinal().atTime(LocalTime.MAX);
            operations.add(match(Criteria.where("invoiceNumber.invoiceDate").gte(start).lte(end)));
        }

        operations.add(unwind("items"));

        List<Criteria> filters = new ArrayList<>();
        if (filter.getRegionalIds() != null && !filter.getRegionalIds().isEmpty()) {
            filters.add(Criteria.where("items.regionalId").in(filter.getRegionalIds()));
        }
        if (filter.getProjectIds() != null && !filter.getProjectIds().isEmpty()) {
            filters.add(Criteria.where("items.projectId").in(filter.getProjectIds()));
        }
        if (filter.getContractIds() != null && !filter.getContractIds().isEmpty()) {
            filters.add(Criteria.where("items.contractId").in(filter.getContractIds()));
        }
        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            filters.add(Criteria.where("items.type").in(filter.getTypes()));
        }

        if (!filters.isEmpty()) {
            operations.add(match(new Criteria().andOperator(filters.toArray(new Criteria[0]))));
        }

        operations.add(Aggregation.addFields().addFieldWithValue("invoiceQty",
            AccumulatorOperators.Sum.sumOf(
                VariableOperators.Map.itemsOf(ConditionalOperators.ifNull("items.invoices").then(Collections.emptyList()))
                    .as("inv")
                    .andApply(ConditionalOperators.ifNull("$$inv.quantity").then(0))
            )
        ).build());

        operations.add(Aggregation.addFields().addFieldWithValue("effectiveQty",
            ConditionalOperators.when(ComparisonOperators.valueOf("invoiceQty").greaterThanValue(0))
                .thenValueOf("invoiceQty")
                .otherwise(ConditionalOperators.ifNull("items.order.quantity").then(1))
        ).build());

        operations.add(Aggregation.addFields().addFieldWithValue("totalValueCalc",
            ArithmeticOperators.Multiply.valueOf(ConditionalOperators.ifNull("items.value").then(0))
                .multiplyBy("effectiveQty")
        ).build());

        operations.add(group("items.groupCode", "items.groupName", "items.idItem", "items.description", "items.type")
            .sum("totalValueCalc").as("totalValue")
            .sum("effectiveQty").as("totalQty")
            .addToSet("items.category").as("statuses"));

        operations.add(project()
            .andExclude("_id")
            .and("_id.groupCode").as("groupCode")
            .and("_id.groupName").as("groupName")
            .and("_id.idItem").as("itemCode")
            .and("_id.description").as("itemName")
            .and("_id.type").as("type")
            .andInclude("totalValue", "totalQty")
            .and(ConditionalOperators.when(ComparisonOperators.valueOf("totalQty").greaterThanValue(0))
                .then(ArithmeticOperators.Divide.valueOf("totalValue").divideBy("totalQty"))
                .otherwise(0)).as("averagePrice")
            .and(ConditionalOperators.ifNull(ArrayOperators.ArrayElemAt.arrayOf("statuses").elementAt(0)).then("NÃO DEFINIDO"))
            .as("status"));

        operations.add(sort(Sort.Direction.DESC, "totalValue"));

        Aggregation aggregation = newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, "purchase_mega", AbcItemBaseRow.class).getMappedResults();
    }
}
