package com.indux.modules.modulo_mega.service;

import com.indux.modules.modulo_mega.application.dto.abc.AbcItemBaseRow;
import com.indux.modules.modulo_mega.application.dto.abc.AbcItemFilter;
import com.indux.modules.modulo_mega.application.dto.abc.AbcItemResponse;
import com.indux.modules.modulo_mega.persistence.repository.abc.PurchaseMegaAbcRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseMegaAbcService {

    private final PurchaseMegaAbcRepositoryCustom repository;

    public List<AbcItemResponse> calculateAbcCurve(AbcItemFilter filter) {
        List<AbcItemBaseRow> items = repository.findAbcItems(filter);
        return classify(items);
    }

    private List<AbcItemResponse> classify(List<AbcItemBaseRow> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<AbcItemBaseRow> sortedItems = items.stream()
                .sorted(Comparator.comparing(AbcItemBaseRow::totalValue, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .toList();

        BigDecimal grandTotal = sortedItems.stream()
                .map(AbcItemBaseRow::totalValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (grandTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return buildWithoutFinancialRelevance(sortedItems);
        }

        List<AbcItemResponse> result = new ArrayList<>();
        BigDecimal accumulatedPercentage = BigDecimal.ZERO;

        for (int i = 0; i < sortedItems.size(); i++) {
            AbcItemBaseRow item = sortedItems.get(i);

            BigDecimal totalValue = defaultValue(item.totalValue());
            BigDecimal totalQty = defaultValue(item.totalQty());
            BigDecimal averagePrice = totalQty.compareTo(BigDecimal.ZERO) > 0
                    ? totalValue.divide(totalQty, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal percentage = totalValue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(grandTotal, 6, RoundingMode.HALF_UP);

            accumulatedPercentage = accumulatedPercentage.add(percentage);

            String curve = resolveCurve(accumulatedPercentage, i);

            result.add(new AbcItemResponse(
                i + 1,
                item.groupCode(),
                item.groupName(),
                item.itemCode(),
                item.itemName(),
                item.type(),
                totalValue.setScale(2, RoundingMode.HALF_UP),
                totalQty.setScale(2, RoundingMode.HALF_UP),
                averagePrice.setScale(2, RoundingMode.HALF_UP),
                item.status(),
                percentage.setScale(2, RoundingMode.HALF_UP),
                accumulatedPercentage.setScale(2, RoundingMode.HALF_UP),
                curve
            ));
        }

        return result;
    }

    private String resolveCurve(BigDecimal accumulatedPercentage, int index) {
        if (index == 0) {
            return "A";
        }

        if (accumulatedPercentage.compareTo(BigDecimal.valueOf(80)) <= 0) {
            return "A";
        }

        if (accumulatedPercentage.compareTo(BigDecimal.valueOf(95)) <= 0) {
            return "B";
        }

        return "C";
    }

    private BigDecimal defaultValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<AbcItemResponse> buildWithoutFinancialRelevance(List<AbcItemBaseRow> items) {
        List<AbcItemResponse> result = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            AbcItemBaseRow item = items.get(i);

            result.add(new AbcItemResponse(
                i + 1,
                item.groupCode(),
                item.groupName(),
                item.itemCode(),
                item.itemName(),
                item.type(),
                defaultValue(item.totalValue()),
                defaultValue(item.totalQty()),
                defaultValue(item.averagePrice()),
                item.status(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "C"
            ));
        }

        return result;
    }
}
