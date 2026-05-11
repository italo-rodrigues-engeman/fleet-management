package com.indux.modules.modulo_mega.service;

import com.indux.modules.modulo_mega.application.dto.*;
import com.indux.modules.modulo_mega.domain.enums.AbcClassificationCriteria;
import com.indux.modules.modulo_mega.domain.repository.specs.AbcQueries;
import com.indux.modules.modulo_mega.domain.repository.AbcItemGateway;
import com.indux.modules.modulo_mega.domain.entity.abc.AbcItem;
import com.indux.modules.modulo_mega.application.mapper.AbcItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AbcCurveService { 

    private final AbcQueries abcQueries; 
    private final AbcItemGateway abcItemGateway;
    private final PdfGeneratorService pdfGeneratorService;
    private final AbcItemMapper mapper; 
    private final ExcelGeneratorService excelGeneratorService;
    
    private static final int ITEMS_DISPLAY_LIMIT = 10;
    
    public byte[] downloadAbcReport(AbcFilterDTO filter) {
        List<AbcCurveGroupDTO> data = calculateAbcCurve(filter, true);
        return pdfGeneratorService.generateAbcCurvePdf(data);
    }

    public byte[] downloadAbcReportExcel(AbcFilterDTO filter) {
        List<AbcCurveGroupDTO> data = calculateAbcCurve(filter, false);
        return excelGeneratorService.generateAbcCurveExcel(data);
    }

    public byte[] downloadAbcReportNew(AbcFilterDTO filter) {
        List<AbcCurveGroupDTO> data = calculateAbcCurveNew(filter, true);
        return pdfGeneratorService.generateAbcCurvePdf(data);
    }

    public byte[] downloadAbcReportExcelNew(AbcFilterDTO filter) {
        List<AbcCurveGroupDTO> data = calculateAbcCurveNew(filter, false);
        return excelGeneratorService.generateAbcCurveExcel(data);
    }

    public List<AbcCurveGroupDTO> calculateAbcCurve(AbcFilterDTO filter, boolean applyLimit) {
        // 1. Busca Dados (View SQL Deprecada)
        List<AbcItemDTO> rawData = abcQueries.findItemsForAbcAnalysis(filter);
        return processAbcCurveItems(rawData, filter, applyLimit);
    }

    public List<AbcCurveGroupDTO> calculateAbcCurveNew(AbcFilterDTO filter, boolean applyLimit) {
        // 1. Busca Dados (MongoDB Aggregation - Clean Architecture)
        List<AbcItem> domainData = abcItemGateway.findItemsForAbcAnalysis(mapper.toDomain(filter));
        List<AbcItemDTO> rawData = domainData != null ? 
            domainData.stream().map(mapper::toDto).collect(Collectors.toList()) : 
            Collections.emptyList();
        
        return processAbcCurveItems(rawData, filter, applyLimit);
    }

    private List<AbcCurveGroupDTO> processAbcCurveItems(List<AbcItemDTO> rawData, AbcFilterDTO filter, boolean applyLimit) {
        if (rawData == null || rawData.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Conversão para DTO
        List<AbcItemDetailDTO> items = mapper.toDetailDtoList(rawData);
        
        // 3. Sanitização Inicial (Nulos viram Zero)
        items.forEach(this::calculateItemMetrics);

        // --- NOVO PASSO: CONSOLIDAÇÃO ---
        // Agrupa itens repetidos (mesmo ID) somando seus valores antes de classificar
        items = consolidateItems(items);
        // --------------------------------
        
        // 4. Definição do Critério
        AbcClassificationCriteria criteria = filter.getCriteria() != null 
            ? filter.getCriteria() 
            : AbcClassificationCriteria.TOTAL_VALUE;

        // 5. Cálculo do Total do Universo
        BigDecimal totalUniverseValue = items.stream()
            .map(item -> extractAnalysisValue(item, criteria))
            .filter(Objects::nonNull) 
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        if (totalUniverseValue.compareTo(BigDecimal.ZERO) == 0) {
            totalUniverseValue = BigDecimal.ONE; 
        }

        final AbcClassificationCriteria selectedCriteria = criteria;

        // 6. Ordenação Decrescente
        items.sort((item1, item2) -> {
            BigDecimal v1 = extractAnalysisValue(item1, selectedCriteria);
            BigDecimal v2 = extractAnalysisValue(item2, selectedCriteria);
            
            BigDecimal safeV1 = (v1 != null) ? v1 : BigDecimal.ZERO;
            BigDecimal safeV2 = (v2 != null) ? v2 : BigDecimal.ZERO;

            int result = safeV2.compareTo(safeV1); 
            
            if (result == 0 && item1.getIdItem() != null && item2.getIdItem() != null) {
                return item1.getIdItem().compareTo(item2.getIdItem());
            }
            return result;
        });

        // 7. Classificação ABC (Regra 80/15/5)
        BigDecimal accumulatedValue = BigDecimal.ZERO;
        BigDecimal limitA = totalUniverseValue.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP); //Regra do negócio: 80%
        BigDecimal limitB = totalUniverseValue.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP); //Regra do negócio : 95%
        
        List<AbcItemDetailDTO> listA = new ArrayList<>();
        List<AbcItemDetailDTO> listB = new ArrayList<>();
        List<AbcItemDetailDTO> listC = new ArrayList<>();

        for (AbcItemDetailDTO item : items) {
            BigDecimal itemValue = extractAnalysisValue(item, criteria);
            if (itemValue == null) itemValue = BigDecimal.ZERO;
            
            if (accumulatedValue.compareTo(limitA) < 0) {
                item.setCalculatedCurveClass("A");
                listA.add(item);
            } else if (accumulatedValue.compareTo(limitB) < 0) {
                item.setCalculatedCurveClass("B");
                listB.add(item);
            } else {
                item.setCalculatedCurveClass("C");
                listC.add(item);
            }

            accumulatedValue = accumulatedValue.add(itemValue);
        }

        // 8. Agrupamento final
        return Arrays.asList(
            createGroup("A", listA, applyLimit),
            createGroup("B", listB, applyLimit),
            createGroup("C", listC, applyLimit)
        );
    }

    // ================= MÉTODOS AUXILIARES =================

    /**
     * Agrupa itens pelo ID, somando Quantidade e Valor Total,
     * e recalculando o Preço Médio.
     */
    private List<AbcItemDetailDTO> consolidateItems(List<AbcItemDetailDTO> rawItems) {
        Map<Integer, AbcItemDetailDTO> consolidatedMap = new HashMap<>();

        for (AbcItemDetailDTO current : rawItems) {
            Integer key = current.getIdItem(); 
            
            if (key == null) continue; 

            consolidatedMap.merge(key, current, (existing, incoming) -> {
                
                BigDecimal totalVal = existing.getTotalValue().add(incoming.getTotalValue());
                existing.setTotalValue(totalVal);

                
                BigDecimal totalQty = existing.getQtdItensTotal().add(incoming.getQtdItensTotal());
                existing.setQtdItensTotal(totalQty);

                
                return existing;
            });
        }

        // Recupera a lista consolidada e recalcula o preço médio ponderado
        List<AbcItemDetailDTO> result = new ArrayList<>(consolidatedMap.values());
        
        for (AbcItemDetailDTO item : result) {
            BigDecimal qty = item.getQtdItensTotal();
            BigDecimal total = item.getTotalValue();

            if (qty != null && qty.compareTo(BigDecimal.ZERO) != 0) {
                
                item.setAveragePrice(total.divide(qty, 2, RoundingMode.HALF_UP));
            } else {
                item.setAveragePrice(BigDecimal.ZERO);
            }
        }

        return result;
    }

    private void calculateItemMetrics(AbcItemDetailDTO item) {
        BigDecimal qty = item.getQtdItensTotal() != null ? item.getQtdItensTotal() : BigDecimal.ZERO;
        BigDecimal total = item.getTotalValue() != null ? item.getTotalValue() : BigDecimal.ZERO;
        BigDecimal avg = item.getAveragePrice() != null ? item.getAveragePrice() : BigDecimal.ZERO;
        
        item.setQtdItensTotal(qty);
        item.setTotalValue(total.setScale(2, RoundingMode.HALF_UP));
        item.setAveragePrice(avg.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal extractAnalysisValue(AbcItemDetailDTO item, AbcClassificationCriteria criteria) {
        return switch (criteria) {
            case QUANTITY -> item.getQtdItensTotal();
            case AVERAGE_PRICE -> item.getAveragePrice();
            case TOTAL_VALUE -> item.getTotalValue();
            default -> item.getTotalValue();
        };
    }
    
    private AbcCurveGroupDTO createGroup(String curveClass, List<AbcItemDetailDTO> allItemsInCurve, boolean applyLimit) {
        if (allItemsInCurve.isEmpty()) {
            return AbcCurveGroupDTO.builder()
                    .curveClass(curveClass)
                    .items(Collections.emptyList())
                    .totalGroupValue(BigDecimal.ZERO)
                    .totalGroupVolume(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal totalValueFullCurve = allItemsInCurve.stream()
                .map(item -> item.getTotalValue() != null ? item.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalQtyFullCurve = allItemsInCurve.stream()
                .map(item -> item.getQtdItensTotal() != null ? item.getQtdItensTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
                
        List<AbcItemDetailDTO> itemsToDisplay = applyLimit 
                ? allItemsInCurve.stream().limit(ITEMS_DISPLAY_LIMIT).collect(Collectors.toList())
                : new ArrayList<>(allItemsInCurve);

        return AbcCurveGroupDTO.builder()
                .curveClass(curveClass)
                .totalGroupValue(totalValueFullCurve)
                .totalGroupVolume(totalQtyFullCurve)
                .items(itemsToDisplay) 
                .build();
    }
}