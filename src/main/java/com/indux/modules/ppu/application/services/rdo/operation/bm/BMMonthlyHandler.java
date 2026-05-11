package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.ppu.*;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.application.dtos.response.bm.BMMonthlyItem;
import com.indux.modules.ppu.application.services.rdo.helper.BMOvertimeServiceHelper;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BMMonthlyHandler {
    private final PPURepository ppuRepository;
    private final RDORepository rdoRepository;
    private final BMMapper mapper;
    private final BMRepository repository;
    private final OvertimeProjectionService overtimeProjectionService;

    public BMMonthlyHandler(PPURepository ppuRepository, RDORepository rdoRepository, BMMapper mapper, BMRepository repository, OvertimeProjectionService overtimeProjectionService) {
        this.ppuRepository = ppuRepository;
        this.rdoRepository = rdoRepository;
        this.mapper = mapper;
        this.repository = repository;
        this.overtimeProjectionService = overtimeProjectionService;
    }


    public List<BMMonthlyItem> fetchMonthlyBM(String bmID) {
        var bm = repository.findById(bmID).orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada."));
        var ppu = ppuRepository
                .findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para esse contrato."));

        DateRange bmPeriod = bm.getPeriod();
        if (bmPeriod == null || bmPeriod.getStart() == null || bmPeriod.getEnd() == null) {
            throw new ModuleNotFoundFailure("BM não possui período de medição definido.");
        }

        var platforms = ppu.getPlatforms();
        var rdos = rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(platforms, RDOStatusOP.APPROVED, bmPeriod.getStart(), bmPeriod.getEnd());
        var result = calculateMonthlyBM(ppu, rdos);
       if(!bm.isFinished()) {
           bm.setResumeMonthly(result);
           repository.save(bm);
       }
        return result;
    }


    private List<BMMonthlyItem> calculateMonthlyBM(PPUEntity ppu, List<RDOEntity> rdos) {

        List<BMMonthlyItem> items = new ArrayList<>();
        var ppuServices = new ArrayList<ServiceLine>();
        if(!ppu.hasAvailableType()){
            ppuServices.addAll(ppu.getServices());
        } else {
            var servicesWithoutAvailable = ppu.getServices().stream()
                    .filter(service -> !service.getDisposicao())
                    .toList();
            ppuServices.addAll(servicesWithoutAvailable);

            var servicesAvailable = ppu.getServices().stream()
                    .filter(ServiceLine::getDisposicao)
                            .toList();

            calcAvailableService(ppu.getAvailableType(), rdos, servicesAvailable, items);
        }

        var serviceQuantities = BMOvertimeServiceHelper.calculateServiceQuantities(rdos, ppuServices, overtimeProjectionService);
        calcService(ppuServices, serviceQuantities, items);
        var ppuEquipments = Optional.ofNullable(ppu.getEquipments()).orElseGet(List::of);
        calcEquipment(rdos, ppuEquipments, items);
        var ppuSteelCables = Optional.ofNullable(ppu.getSteelCables()).orElseGet(List::of);
        calcSteelCable(rdos, ppuSteelCables, items);
        var ppuAccessoryKits = Optional.ofNullable(ppu.getAccessoryKits()).orElseGet(List::of);
        calcAccessoryKits(rdos, ppuAccessoryKits, items);
        var ppuPureline = Optional.ofNullable(ppu.getLines()).orElseGet(List::of);
        calcPureLine(rdos, ppuPureline, items);
        sortByPpuServiceOrder(ppu, items);
        return items;
    }

    private void calcAvailableService(
            List<AvailableType> types,
            List<RDOEntity> rdos,
            List<ServiceLine> ppuServices,
            List<BMMonthlyItem> items
    ) {
        if (ppuServices == null || ppuServices.isEmpty()) return;

        var typesByName = Optional.ofNullable(types).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getName() != null && !t.getName().isBlank())
                .collect(Collectors.toMap(
                        AvailableType::getName,
                        AvailableType::getFactor,
                        (a, b) -> a
                ));

        var base = Optional.ofNullable(rdos).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .flatMap(r -> Optional.ofNullable(r.getServices()).orElseGet(List::of).stream())
                .filter(Objects::nonNull)
                .filter(s -> s.getServiceID() != null && !s.getServiceID().isBlank())
                .toList();

        for (var service : ppuServices) {
            var records = base.stream()
                    .filter(s -> service.getId().equals(s.getServiceID()))
                    .toList();


            var value = BigDecimal.valueOf(service.getValue());
            var defaultFactor = service.getFactor() != null ? BigDecimal.valueOf(service.getFactor()) : BigDecimal.ONE;

            BigDecimal quantity = BigDecimal.ZERO;
            BigDecimal total = BigDecimal.ZERO;

            for (var rec : records) {
                var q = BigDecimal.ONE;
                quantity = quantity.add(q);

                BigDecimal factorToUse = defaultFactor;

                var availableName = rec.getAvailableType();
                if (availableName != null && !availableName.isBlank()) {
                    var availableFactor = typesByName.get(availableName);
                    if (availableFactor != null) {
                        factorToUse = BigDecimal.valueOf(availableFactor);
                    }
                }

                total = total.add(value.multiply(factorToUse).multiply(q));
            }

            var item = mapper.toBMMonthlyItem(service);
            item.setQuantity(round2(quantity.doubleValue()));
            item.setTotal(round2(total));
            items.add(item);
        }
    }


    private void calcAccessoryKits(List<RDOEntity> rdos, List<AccessoryKitLine> ppuAccessoryKits, List<BMMonthlyItem> items) {
        Map<String, Double> accessoryKitQuantities = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getAccessoryKits()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        AccessoryKitDTO::lineID,
                        Collectors.summingDouble(k -> Optional.ofNullable(k.quantidade()).orElse(0.0))
                ));

        for (var kit : ppuAccessoryKits) {
            var item = mapper.toBMMonthlyItem(kit);

            var monthlyQuantity = accessoryKitQuantities.getOrDefault(kit.getId(), 0.0);
            item.setQuantity(round2(monthlyQuantity));

            BigDecimal unitValue = BigDecimal.valueOf(kit.getValue());
            BigDecimal total = unitValue.multiply(BigDecimal.valueOf(monthlyQuantity));
            item.setTotal(round2(total));

            items.add(item);
        }
    }

    private void calcPureLine(List<RDOEntity> rdos, List<LinePPU> ppuPureline, List<BMMonthlyItem> items) {
        Map<String, Double> accessoryKitQuantities = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getLines()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        RDOLine::getParentId,
                        Collectors.summingDouble(line -> Optional.ofNullable(line.getValueMeasured()).orElse(0.0))
                ));

        for (var line : ppuPureline) {
            var item = mapper.toBMMonthlyItem(line);

            var monthlyQuantity = accessoryKitQuantities.getOrDefault(line.getId(), 0.0);
            item.setQuantity(round2(monthlyQuantity));

            BigDecimal unitValue = BigDecimal.valueOf(line.getValue());
            BigDecimal total = unitValue.multiply(BigDecimal.valueOf(monthlyQuantity));
            item.setTotal(round2(total));

            items.add(item);
        }
    }

    private void calcSteelCable(List<RDOEntity> rdos, List<SteelCableLine> ppuSteelCables, List<BMMonthlyItem> items) {
        Map<String, Double> steelCableQuantities = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getSteelCable()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        SteelCableChecker::lineID,
                        Collectors.summingDouble(c -> Optional.ofNullable(c.quantidade()).orElse(0.0))
                ));

        for (var cable : ppuSteelCables) {
            var item = mapper.toBMMonthlyItem(cable);

            var monthlyQuantity = steelCableQuantities.getOrDefault(cable.getId(), 0.0);
            item.setQuantity(round2(monthlyQuantity));

            BigDecimal unitValue = BigDecimal.valueOf(cable.getValue());
            BigDecimal total = unitValue.multiply(BigDecimal.valueOf(monthlyQuantity));
            item.setTotal(round2(total));

            items.add(item);
        }
    }

    private void calcService(List<ServiceLine> ppuServices, Map<String, Double> serviceQuantities, List<BMMonthlyItem> items) {
        if (ppuServices == null || ppuServices.isEmpty()) return;

        var quantities = serviceQuantities != null ? serviceQuantities : Map.<String, Double>of();

        for (var service : ppuServices) {

            var monthlyQuantity = quantities.getOrDefault(service.getId(), 0.0);
            items.add(createBMMonthlyItem(service, monthlyQuantity));
        }
    }

    private BMMonthlyItem createBMMonthlyItem(ServiceLine service, double quantity) {
        var item = mapper.toBMMonthlyItem(service);
        item.setQuantity(round2(quantity));

        var factor = service.getFactor() != null ? BigDecimal.valueOf(service.getFactor()) : BigDecimal.ONE;
        var unitValue = BigDecimal.valueOf(service.getValue()).multiply(factor);
        var total = unitValue.multiply(BigDecimal.valueOf(quantity));
        item.setTotal(round2(total));

        return item;
    }

    private void calcEquipment(List<RDOEntity> rdos, List<EquipmentLine> ppuEquipments, List<BMMonthlyItem> items) {
        if (rdos.isEmpty() || ppuEquipments.isEmpty()) return;

        Map<String, Integer> equipmentQuantities = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getEquipments()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        RDOEquipment::getEquipmentPPUId,
                        Collectors.summingInt(e ->
                                (int) Optional.ofNullable(e.getCheckers()).orElseGet(List::of).stream()
                                        .filter(c -> Boolean.TRUE.equals(c.operacional()))
                                        .count()
                        )
                ));

        for (var equip : ppuEquipments) {
            var item = mapper.toBMMonthlyItem(equip);

            int monthlyQuantity = equipmentQuantities.getOrDefault(equip.getId(), 0);

            var factor = equip.getFactor() != null ? BigDecimal.valueOf(equip.getFactor()) : BigDecimal.ONE;
            BigDecimal unitValue = BigDecimal.valueOf(equip.getValue()).multiply(factor);
            BigDecimal total = unitValue.multiply(BigDecimal.valueOf(monthlyQuantity));
            item.setTotal(round2(total));
            item.setQuantity(round2(monthlyQuantity));

            items.add(item);
        }
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal round2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private void sortByPpuServiceOrder(PPUEntity ppu, List<BMMonthlyItem> items) {
        var services = Optional.ofNullable(ppu.getServices()).orElseGet(List::of);

        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < services.size(); i++) {
            index.put(services.get(i).getId(), i);
        }

        items.sort((a, b) -> {
            boolean aIsService = index.containsKey(a.getId());
            boolean bIsService = index.containsKey(b.getId());

            if (aIsService && bIsService) {
                return Integer.compare(index.get(a.getId()), index.get(b.getId()));
            }
            if (aIsService) return -1;
            if (bIsService) return 1;
            return 0;
        });
    }

}