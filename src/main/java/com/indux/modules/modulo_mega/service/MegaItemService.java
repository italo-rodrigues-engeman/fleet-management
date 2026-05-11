package com.indux.modules.modulo_mega.service;

import com.indux.modules.modulo_mega.application.dto.*;
import com.indux.modules.modulo_mega.application.mapper.GlobalHighlightsMapper;
import com.indux.modules.modulo_mega.application.mapper.ItemMapper;
import com.indux.modules.modulo_mega.domain.logic.MegaOrderBusinessRules;
import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntityInvoice;
import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntityOrder;
import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntityOrganogram;
import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntitySolicitation;
import com.indux.modules.modulo_mega.domain.repository.MegaEntityInvoiceRepository;
import com.indux.modules.modulo_mega.domain.repository.MegaEntityOrganogramRepository;
import com.indux.modules.modulo_mega.domain.repository.MegaEntitySolicitationRepository;
import com.indux.modules.modulo_mega.domain.repository.specs.MegaItemQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MegaItemService {

    private final MegaEntityOrganogramRepository organogramRepository;
    private final MegaEntitySolicitationRepository solicitationRepository;
    private final MegaEntityInvoiceRepository invoiceRepository;
    private final MegaItemQueries megaItemQueries;
    private final GlobalHighlightsMapper mapper;
    private final ItemMapper itemMapper;
    @PersistenceContext
    private EntityManager em;

    // =================================================================================
    // MÉTODOS PÚBLICOS
    // =================================================================================
    public Page<ItemGroupResponseDTO> findItemsByGroup(
        List<Integer> groupCodes, 
        List<String> groupNames, 
        String term, 
        Pageable pageable) {
    
        return megaItemQueries.findItemsByGroupFilters(groupCodes, groupNames, term, pageable);
    }
    public Page<ItemGroupedDTO> searchItems(ItemFilter filter, Pageable pageable) {
        if (filter.getCadastroEndDate() == null) {
            filter.setCadastroEndDate(LocalDate.now());
        }
        return megaItemQueries.searchItemsGrouped(filter, pageable);
    }

    public Page<ItemHistoryDetailedDTO> getItemHistoryDetails(Integer idItem, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (idItem == null) return Page.empty(pageable);
        LocalDate finalEndDate = (endDate != null) ? endDate : LocalDate.now();
        return megaItemQueries.findItemHistoryDetails(idItem, startDate, finalEndDate, pageable);
    }

    public ItemDetailedDTO getItemDetailedInfo(Integer idItem, LocalDate startDate, LocalDate endDate) {
        // 1. Limpa o contexto de persistência antes de começar
        em.clear();

        LocalDate finalEndDate = (endDate != null) ? endDate : LocalDate.now();
        List<MegaEntityOrder> items = megaItemQueries.findByitemIdAndDateRange(idItem, startDate, finalEndDate);

        if (items == null || items.isEmpty()) {
            return new ItemDetailedDTO();
        }
        return calculateLogic(items);
    }

    // =================================================================================
    // LÓGICA DE CONSTRUÇÃO OTIMIZADA
    // =================================================================================

    private ItemDetailedDTO calculateLogic(List<MegaEntityOrder> items) {
        return Optional.ofNullable(items)
            .map(Collection::stream) // Transforma a lista em stream (se não for nula)
            .orElseGet(Stream::empty)
            .filter(Objects::nonNull) // Garante que o primeiro item não seja null
            .findFirst()
            .map(firstItem -> {
                // Se encontrar o primeiro item, executa a lógica principal
                Integer currentIdItem = firstItem.getIdItem();

                // 1. Mapa de Organograma
                Map<Object, MegaEntityOrganogram> organogramMap = organogramRepository.findAll().stream()
                    .filter(h -> h.getProjectCode() != null)
                    .collect(Collectors.toMap(
                        MegaEntityOrganogram::getProjectCode,
                        Function.identity(),
                        (existing, replacement) -> existing
                    ));

                // 2. Busca de dados vinculados (Solicitações e Notas)
                Map<Integer, List<MegaEntitySolicitation>> solicitationMap = solicitationRepository.findByIdItem(currentIdItem)
                    .stream()
                    .filter(s -> s.getOrderNumber() != null)
                    .collect(Collectors.groupingBy(MegaEntitySolicitation::getOrderNumber));

                Map<Integer, List<MegaEntityInvoice>> invoiceMap = invoiceRepository.findByIdItem(currentIdItem)
                    .stream()
                    .filter(i -> i.getOrderNumber() != null)
                    .collect(Collectors.groupingBy(MegaEntityInvoice::getOrderNumber));

                ItemDetailedDTO dto = createBaseDto(firstItem);
                
                calculatePurchaseDates(dto, items, organogramMap, solicitationMap, invoiceMap);
                processGlobalStats(dto, items, organogramMap, solicitationMap, invoiceMap);

                // Uso de Optional também para extrair o ID de informação básica com segurança
                Integer code = Optional.ofNullable(dto.getBasicInformation())
                    .map(info -> info.getIdItem())
                    .orElse(null);

                List<RegionalDetailDTO> regionals = buildRegionalList(items, code, organogramMap, solicitationMap, invoiceMap);
                dto.setRegionalDetails(regionals);

                return dto;
            })
            .orElseGet(ItemDetailedDTO::new); // Se a lista for nula/vazia/apenas com nulls, retorna novo DTO
        }

    private ItemDetailedDTO createBaseDto(MegaEntityOrder first) {
        ItemDetailedDTO dto = itemMapper.toDetailedDto(first);
        if (dto.getDates() == null) dto.setDates(new ItemDetailedDTO.Dates());
        if (dto.getBasicInformation() == null) dto.setBasicInformation(new ItemDetailedDTO.BasicInformation());
        if (dto.getTotals() == null) dto.setTotals(new ItemDetailedDTO.Totals());
        if (dto.getPriceAnalysis() == null) dto.setPriceAnalysis(new ItemDetailedDTO.PriceAnalysis());
        return dto;
    }

    private void processGlobalStats(ItemDetailedDTO dto, List<MegaEntityOrder> items, 
                                    Map<Object, MegaEntityOrganogram> organogramMap,
                                    Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                    Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        RegionalDetailDTO globalStats = new RegionalDetailDTO();
        calculateAndFillStats(items, globalStats, organogramMap, solicitationMap, invoiceMap);

        if (dto.getTotals() != null && globalStats.getTotals() != null) {
            dto.getTotals().setTotalqtdItensPurchased(globalStats.getTotals().getTotalqtdItens());
            dto.getTotals().setTotalValueSpent(globalStats.getTotals().getTotalValue());
            dto.getTotals().setTotalOrdersGeneral(globalStats.getTotals().getTotalOrders());
            dto.getTotals().setAveragePrice(calculateAveragePrice(items));
        }

        if (globalStats.getPriceAnalysis() != null) {
            dto.setPriceAnalysis(convertGroupStatsToItemPriceAnalysis(globalStats.getPriceAnalysis()));
        }
        mapper.fillGlobalHighlights(dto, globalStats);
    }

    private ItemDetailedDTO.PriceAnalysis convertGroupStatsToItemPriceAnalysis(BaseStatisticsDTO.GroupPriceAnalysis groupAnalysis) {
        ItemDetailedDTO.PriceAnalysis analysis = new ItemDetailedDTO.PriceAnalysis();
        if (groupAnalysis.getMinimumPrice() != null) analysis.setMinimumPrice(mapDetail(groupAnalysis.getMinimumPrice()));
        if (groupAnalysis.getMaximumPrice() != null) analysis.setMaximumPrice(mapDetail(groupAnalysis.getMaximumPrice()));
        if (groupAnalysis.getAveragePrice() != null) analysis.setAveragePrice(mapDetail(groupAnalysis.getAveragePrice()));
        return analysis;
    }

    private ItemDetailedDTO.PriceDetail mapDetail(BaseStatisticsDTO.GroupPriceDetail source) {
        ItemDetailedDTO.PriceDetail target = new ItemDetailedDTO.PriceDetail();
        target.setValue(source.getValue());
        target.setSupplier(source.getSupplier());
        target.setDate(source.getDate());
        target.setInclusionUser(source.getInclusionUser());
        target.setEditionUser(source.getAlterationUser());
        target.setRequester(source.getRequester());
        target.setBuyer(source.getBuyer());
        target.setBranch(source.getBranch());
        target.setRegional(source.getRegional());
        target.setUnitOfMeasure(source.getUnitOfMeasure());
        target.setSolicitation(source.getSolicitation());
        target.setApprove(source.getApprove());
        target.setOrderNumber(source.getOrderNumber());
        target.setOrderType(source.getOrderType()); 
        return target;
    }

    private void calculateAndFillStats(List<MegaEntityOrder> items, Object targetDto, 
                                       Map<Object, MegaEntityOrganogram> organogramMap,
                                       Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                       Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        BigDecimal avgPrice = calculateAveragePrice(items);
        if (targetDto instanceof RegionalDetailDTO) {
            calculateStatsForGroup(items, (RegionalDetailDTO) targetDto, avgPrice, organogramMap, solicitationMap, invoiceMap);
        } else if (targetDto instanceof BranchDetailDTO) {
            calculateStatsForGroup(items, (BranchDetailDTO) targetDto, avgPrice, organogramMap, solicitationMap, invoiceMap);
        }
    }

    enum StatType { MIN, MAX, AVG }

    private void calculateStatsForGroup(List<MegaEntityOrder> groupItems, BaseStatisticsDTO target, BigDecimal referenceAvg, 
                                         Map<Object, MegaEntityOrganogram> organogramMap,
                                         Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                         Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        if (target.getTotals() == null) target.setTotals(new BaseStatisticsDTO.GroupTotals());

        target.getTotals().setTotalqtdItens(sum(groupItems, MegaEntityOrder::getQtdItensTotal));
        target.getTotals().setTotalValue(sum(groupItems, MegaEntityOrder::getTotalItemValue));
        long countOrders = groupItems.stream().filter(i -> i.getIdItem() != null).count();
        target.getTotals().setTotalOrders(BigDecimal.valueOf(countOrders));

        List<MegaEntityOrder> itemsWithPrice = groupItems.stream().filter(i -> i.getTotalItemValue() != null).toList();
        if (itemsWithPrice.isEmpty()) return;

        if (target.getPriceAnalysis() == null) target.setPriceAnalysis(new BaseStatisticsDTO.GroupPriceAnalysis());

        Comparator<MegaEntityOrder> qualityTieBreaker = Comparator
                .comparing((MegaEntityOrder i) -> i.getOrderDate() != null ? 1 : 0)
                .thenComparing(i -> i.getSupplierName() != null ? 1 : 0)
                .thenComparing(MegaEntityOrder::getId, Comparator.reverseOrder());

        BigDecimal minVal = itemsWithPrice.stream().map(MegaEntityOrder::getAveragePriceFromSupplier).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        itemsWithPrice.stream().filter(i -> i.getAveragePriceFromSupplier().compareTo(minVal) == 0).max(qualityTieBreaker)
                .ifPresent(item -> mapItemToTarget(target, item, StatType.MIN, organogramMap, solicitationMap, invoiceMap));

        BigDecimal maxVal = itemsWithPrice.stream().map(MegaEntityOrder::getAveragePriceFromSupplier).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        itemsWithPrice.stream().filter(i -> i.getAveragePriceFromSupplier().compareTo(maxVal) == 0).max(qualityTieBreaker)
                .ifPresent(item -> mapItemToTarget(target, item, StatType.MAX, organogramMap, solicitationMap, invoiceMap));

        BigDecimal totalValue = sum(itemsWithPrice, MegaEntityOrder::getTotalItemValue);
        BigDecimal totalQty = sum(itemsWithPrice, MegaEntityOrder::getQtdItensTotal);
        BigDecimal avgVal = (totalQty != null && totalQty.compareTo(BigDecimal.ZERO) > 0) ? totalValue.divide(totalQty, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BaseStatisticsDTO.GroupPriceDetail avgDetail = new BaseStatisticsDTO.GroupPriceDetail();
        avgDetail.setValue(avgVal);
        target.getPriceAnalysis().setAveragePrice(avgDetail);
    }

    private void mapItemToTarget(BaseStatisticsDTO target, MegaEntityOrder item, StatType type, 
                                 Map<Object, MegaEntityOrganogram> organogramMap,
                                 Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                 Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        
        BaseStatisticsDTO.GroupPriceDetail detail = new BaseStatisticsDTO.GroupPriceDetail();
        
        detail.setValue(item.getAveragePriceFromSupplier());
        detail.setSupplier(item.getSupplierName());
        detail.setDate(item.getOrderDate());
        detail.setBuyer(item.getBuyerName());
        detail.setBranch(item.getBranchName());
        detail.setUnitOfMeasure(item.getUnitOfMeasure());
        detail.setOrderNumber(item.getOrderNumber());
        
        detail.setApprove(item.getApprove());

        Integer orderKey = item.getOrderNumber();
        boolean hasOrder = orderKey != null && orderKey != 0;
        boolean hasNF = item.getNoteNumber() != null && !item.getNoteNumber().trim().isEmpty();
        boolean hasAP = item.getApprove() != null && !item.getApprove().trim().isEmpty();
        boolean hasSol = item.getSolicitation() != null && item.getSolicitation() != 0;

        
        List<MegaEntitySolicitation> sols = solicitationMap.get(item.getOrderNumber() != null ? item.getOrderNumber() : null);
            if (sols != null && !sols.isEmpty()) {
               
                detail.setRequester(sols.get(0).getRequesterName());
                detail.setSolicitation(sols.get(0).getSolicitation());
            }

            
        
        
        // CHAMADA DA LÓGICA CENTRALIZADA
        detail.setOrderType(MegaOrderBusinessRules.calculateOrderType(hasSol, hasOrder, hasNF, hasAP));

        MegaEntityOrganogram hierarchy = resolveHierarchy(item, organogramMap);
        if (hierarchy != null) detail.setRegional(hierarchy.getRegionalName());

        detail.setInclusionUser(toStringOrNull(item.getRegisterUser()));
        detail.setAlterationUser(toStringOrNull(item.getRegisterUserEdition()));

        switch (type) {
            case MIN -> target.getPriceAnalysis().setMinimumPrice(detail);
            case MAX -> target.getPriceAnalysis().setMaximumPrice(detail);
            case AVG -> target.getPriceAnalysis().setAveragePrice(detail);
        }
    }

    private MegaEntityOrganogram resolveHierarchy(MegaEntityOrder item, Map<Object, MegaEntityOrganogram> map) {
        if (item == null || item.getProjectCode() == null || map == null) return null;
        return map.get(item.getProjectCode());
    }

    private ItemDetailedDTO.PriceDetail createDetailFromItem(MegaEntityOrder item, 
                                                             Map<Object, MegaEntityOrganogram> organogramMap,
                                                             Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                                             Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        if (item == null) return null;
        ItemDetailedDTO.PriceDetail target = new ItemDetailedDTO.PriceDetail();

        target.setValue(item.getAveragePriceFromSupplier());
        target.setSupplier(item.getSupplierName());
        target.setDate(item.getOrderDate() != null ? item.getOrderDate() : item.getInvoiceDate());
        target.setBuyer(item.getBuyerName());
        target.setBranch(item.getBranchName());
        
        target.setApprove(item.getApprove());
        target.setOrderNumber(item.getOrderNumber());
        target.setUnitOfMeasure(item.getUnitOfMeasure());

        Integer orderKey = item.getOrderNumber();
        boolean hasOrder = orderKey != null && orderKey != 0;
        boolean hasNF = item.getNoteNumber() != null && !item.getNoteNumber().trim().isEmpty();
        boolean hasAP = item.getApprove() != null && !item.getApprove().trim().isEmpty();
        boolean hasSol = false;

        
            List<MegaEntitySolicitation> sols = solicitationMap.get(item.getOrderNumber() != null ? item.getOrderNumber() : null);
            if (sols != null && !sols.isEmpty()) {
                hasSol = true;
                target.setRequester(sols.get(0).getRequesterName());
                target.setSolicitation(sols.get(0).getSolicitation());
            }

            
        
        
        // CHAMADA DA LÓGICA CENTRALIZADA
        target.setOrderType(MegaOrderBusinessRules.calculateOrderType(hasSol, hasOrder, hasNF, hasAP));
        
        MegaEntityOrganogram hierarchy = resolveHierarchy(item, organogramMap);
        if (hierarchy != null) target.setRegional(hierarchy.getRegionalName());
        return target;
    }

    private List<RegionalDetailDTO> buildRegionalList(List<MegaEntityOrder> items, Integer idItem, 
                                                      Map<Object, MegaEntityOrganogram> organogramMap,
                                                      Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                                      Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        return items.stream()
                .collect(Collectors.groupingBy(item -> {
                    MegaEntityOrganogram h = resolveHierarchy(item, organogramMap);
                    return (h != null && h.getRegionalCode() != null) ? String.valueOf(h.getRegionalCode()) : " - ";
                }))
                .entrySet().stream()
                .map(entry -> createRegionalDto(entry.getKey(), entry.getValue(), idItem, organogramMap, solicitationMap, invoiceMap))
                .sorted(Comparator.comparing(RegionalDetailDTO::getRegionalCode))
                .collect(Collectors.toList());
    }

    private RegionalDetailDTO createRegionalDto(String regionalCode, List<MegaEntityOrder> regionalItems, Integer idItem, 
                                                Map<Object, MegaEntityOrganogram> organogramMap,
                                                Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                                Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        RegionalDetailDTO regionalDto = new RegionalDetailDTO();
        regionalDto.setRegionalCode(regionalCode);
        if (!regionalItems.isEmpty()) {
             MegaEntityOrganogram h = resolveHierarchy(regionalItems.get(0), organogramMap);
             if (h != null && h.getRegionalName() != null) regionalDto.setRegionalName(h.getRegionalName());
        }
        calculateAndFillStats(regionalItems, regionalDto, organogramMap, solicitationMap, invoiceMap);
        calculateRegionalPurchaseDates(regionalDto, regionalItems, organogramMap, solicitationMap, invoiceMap);
        regionalDto.setBranches(buildBranchList(regionalItems, organogramMap, solicitationMap, invoiceMap));
        return regionalDto;
    }

    private List<BranchDetailDTO> buildBranchList(List<MegaEntityOrder> regionalItems, 
                                                  Map<Object, MegaEntityOrganogram> organogramMap,
                                                  Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                                  Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        return regionalItems.stream()
            .collect(Collectors.groupingBy(item -> item.getBranchId() != null ? item.getBranchId() : "SEM_FILIAL"))
            .entrySet().stream()
            .map(entry -> {
                BranchDetailDTO branchDto = new BranchDetailDTO();
                branchDto.setBranchId(String.valueOf(entry.getKey()));
                if (!entry.getValue().isEmpty()) branchDto.setBranchName(entry.getValue().get(0).getBranchName());
                calculateAndFillStats(entry.getValue(), branchDto, organogramMap, solicitationMap, invoiceMap);
                return branchDto;
            })
            .sorted(Comparator.comparing(BranchDetailDTO::getBranchId))
            .collect(Collectors.toList());
    }

    private void calculatePurchaseDates(ItemDetailedDTO dto, List<MegaEntityOrder> itemRecords, 
                                        Map<Object, MegaEntityOrganogram> organogramMap,
                                        Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                        Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        if (itemRecords == null || itemRecords.isEmpty()) return;
        Function<MegaEntityOrder, LocalDate> dateStrategy = determineDateStrategy(itemRecords);
        List<MegaEntityOrder> validDateItems = itemRecords.stream().filter(i -> dateStrategy.apply(i) != null).toList();
        if (validDateItems.isEmpty()) return;

        Comparator<MegaEntityOrder> naturalOrderComparator = Comparator.comparing(dateStrategy).thenComparing(MegaEntityOrder::getId);
        MegaEntityOrder firstPurchaseItem = validDateItems.stream().min(naturalOrderComparator).orElse(null);
        MegaEntityOrder lastPurchaseItem = validDateItems.stream().max(naturalOrderComparator).orElse(null);

        if (dto.getDates() != null) {
            if (firstPurchaseItem != null) {
                LocalDate date = dateStrategy.apply(firstPurchaseItem);
                dto.getDates().setFirstPurchaseDate(date);
                ItemDetailedDTO.PriceDetail detail = createDetailFromItem(firstPurchaseItem, organogramMap, solicitationMap, invoiceMap);
                if (detail != null) detail.setDate(date);
                dto.getDates().setFirstPurchaseDetail(detail);
            }
            if (lastPurchaseItem != null) {
                LocalDate date = dateStrategy.apply(lastPurchaseItem);
                dto.getDates().setLastPurchaseDate(date);
                ItemDetailedDTO.PriceDetail detail = createDetailFromItem(lastPurchaseItem, organogramMap, solicitationMap, invoiceMap);
                if (detail != null) detail.setDate(date);
                dto.getDates().setLastPurchaseDetail(detail);
            }
        }
    }

    private BigDecimal sum(List<MegaEntityOrder> items, Function<MegaEntityOrder, BigDecimal> mapper) {
        return items.stream().map(mapper).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculateRegionalPurchaseDates(RegionalDetailDTO dto, List<MegaEntityOrder> regionalItems, 
                                                Map<Object, MegaEntityOrganogram> organogramMap,
                                                Map<Integer, List<MegaEntitySolicitation>> solicitationMap,
                                                Map<Integer, List<MegaEntityInvoice>> invoiceMap) {
        if (regionalItems == null || regionalItems.isEmpty()) return;
        Function<MegaEntityOrder, LocalDate> dateStrategy = determineDateStrategy(regionalItems);
        List<MegaEntityOrder> validDateItems = regionalItems.stream().filter(i -> dateStrategy.apply(i) != null).toList();
        
        if (dto.getDates() == null) dto.setDates(new ItemDetailedDTO.Dates());
        if (!regionalItems.isEmpty()) dto.getDates().setCreationDate(regionalItems.get(0).getCreationDate());
        if (validDateItems.isEmpty()) return;

        Comparator<MegaEntityOrder> naturalOrderComparator = Comparator.comparing(dateStrategy).thenComparing(MegaEntityOrder::getId);
        MegaEntityOrder firstPurchaseItem = validDateItems.stream().min(naturalOrderComparator).orElse(null);
        MegaEntityOrder lastPurchaseItem = validDateItems.stream().max(naturalOrderComparator).orElse(null);

        if (firstPurchaseItem != null) {
            LocalDate date = dateStrategy.apply(firstPurchaseItem);
            dto.getDates().setFirstPurchaseDate(date);
            ItemDetailedDTO.PriceDetail detail = createDetailFromItem(firstPurchaseItem, organogramMap, solicitationMap, invoiceMap);
            if (detail != null) detail.setDate(date);
            dto.getDates().setFirstPurchaseDetail(detail);
        }
        if (lastPurchaseItem != null) {
            LocalDate date = dateStrategy.apply(lastPurchaseItem);
            dto.getDates().setLastPurchaseDate(date);
            ItemDetailedDTO.PriceDetail detail = createDetailFromItem(lastPurchaseItem, organogramMap, solicitationMap, invoiceMap);
            if (detail != null) detail.setDate(date);
            dto.getDates().setLastPurchaseDetail(detail);
        }
    }

    private String toStringOrNull(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private BigDecimal calculateAveragePrice(List<MegaEntityOrder> items) {
        List<BigDecimal> prices = items.stream().map(MegaEntityOrder::getTotalItemValue).filter(Objects::nonNull).toList();
        if (prices.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sumPrice = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumPrice.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
    }

    private Function<MegaEntityOrder, LocalDate> determineDateStrategy(List<MegaEntityOrder> items) {
        boolean hasAnyOrder = items.stream().anyMatch(i -> i.getOrderDate() != null);
        return hasAnyOrder ? MegaEntityOrder::getOrderDate : MegaEntityOrder::getInvoiceDate;
    }
}