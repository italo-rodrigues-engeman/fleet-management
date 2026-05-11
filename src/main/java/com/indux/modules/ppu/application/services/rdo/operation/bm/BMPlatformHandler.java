package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.response.bm.BMPlatformReport;
import com.indux.modules.ppu.application.services.rdo.helper.BMOvertimeServiceHelper;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.entities.bm.BMContext;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BMPlatformHandler {
    private final PPURepository ppuRepository;
    private final RDORepository rdoRepository;
    private final BMRepository repository;
    private final OvertimeProjectionService overtimeProjectionService;

    public BMPlatformHandler(PPURepository ppuRepository, RDORepository rdoRepository, BMRepository repository, OvertimeProjectionService overtimeProjectionService) {
        this.ppuRepository = ppuRepository;
        this.rdoRepository = rdoRepository;
        this.repository = repository;
        this.overtimeProjectionService = overtimeProjectionService;
    }


    public List<BMPlatformReport> fetchAll(String bmID) {
        var bm = repository.findById(bmID).orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada pelo ID informado."));
        var ppu = getPPU(bm.getProjectId());

        DateRange bmPeriod = bm.getPeriod();
        if (bmPeriod == null || bmPeriod.getStart() == null || bmPeriod.getEnd() == null) {
            throw new ModuleFailure("BM não possui período de medição definido.");
        }

        var platforms = ppu.getPlatforms();
        var rdos = rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(platforms, RDOStatusOP.APPROVED, bmPeriod.getStart(), bmPeriod.getEnd());

        return getBmPlatformReports(bm, rdos, ppu);
    }

    public List<BMPlatformReport> fetchAll(BMEntity bm, List<RDOEntity> rdos) {
        var ppu = getPPU(bm.getProjectId());
        DateRange bmPeriod = bm.getPeriod();
        if (bmPeriod == null || bmPeriod.getStart() == null || bmPeriod.getEnd() == null) {
            throw new ModuleNotFoundFailure("BM não possui período de medição definido.");
        }

        return getBmPlatformReports(bm, rdos, ppu);
    }

    private PPUEntity getPPU(Long projectID){
      return ppuRepository
              .findByProjectIdAndStatus(projectID, DocumentStatus.ABERTO)
              .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para esse contrato."));
    }

    @NotNull
    private List<BMPlatformReport> getBmPlatformReports(BMEntity bm, List<RDOEntity> rdos, PPUEntity ppu) {
        BMContext context = new BMContext(bm.getPeriod().getStart(), bm.getPeriod().getEnd(), "*", bm.getProjectId());
        context.setDaysInQuery((int) ChronoUnit.DAYS.between(bm.getPeriod().getStart(), bm.getPeriod().getEnd()) + 1);
        context.setPlatforms(ppu.getPlatforms());
        var result = calculateByPlatformTotals(ppu, rdos, context);
        if(!bm.isFinished()) {
            bm.setResumePlatforms(result);
            repository.save(bm);
        }
        return result;
    }


    private List<BMPlatformReport> calculateByPlatformTotals(PPUEntity ppu, List<RDOEntity> rdos, BMContext context) {
        List<BMPlatformReport> results = new ArrayList<>();

        Map<String, List<RDOEntity>> rdosByPlatform = rdos.stream()
                .collect(Collectors.groupingBy(RDOEntity::getPlatform));

        var allPpuServices = Optional.ofNullable(ppu.getServices()).orElseGet(List::of);
        var ppuEquipments = Optional.ofNullable(ppu.getEquipments()).orElseGet(List::of);
        var ppuSteelCables = Optional.ofNullable(ppu.getSteelCables()).orElseGet(List::of);
        var ppuAccessoryKits = Optional.ofNullable(ppu.getAccessoryKits()).orElseGet(List::of);
        var ppuPureLines = Optional.ofNullable(ppu.getLines()).orElseGet(List::of);

        var hasAvailable = ppu.hasAvailableType();

        List<ServiceLine> servicesWithoutAvailable;
        List<ServiceLine> servicesAvailable;
        Map<String, Double> availableFactorByName;

        if (!hasAvailable) {
            servicesWithoutAvailable = allPpuServices;
            servicesAvailable = List.of();
            availableFactorByName = Map.of();
        } else {
            servicesWithoutAvailable = allPpuServices.stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getDisposicao()))
                    .toList();

            servicesAvailable = allPpuServices.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getDisposicao()))
                    .toList();

            availableFactorByName = Optional.ofNullable(ppu.getAvailableType()).orElseGet(List::of).stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.getName() != null && !t.getName().isBlank())
                    .collect(Collectors.toMap(
                            AvailableType::getName,
                            t -> t.getFactor() != null ? t.getFactor() : 1.0,
                            (a, b) -> a
                    ));
        }

        for (String platform : context.getPlatforms()) {
            var platformRdos = rdosByPlatform.getOrDefault(platform, List.of());

            Map<String, Double> serviceCounts =
                    BMOvertimeServiceHelper.calculateServiceQuantities(platformRdos, servicesWithoutAvailable, overtimeProjectionService);

            Map<String, Integer> equipmentCounts = platformRdos.stream()
                    .flatMap(r -> Optional.ofNullable(r.getEquipments()).orElseGet(List::of).stream())
                    .collect(Collectors.groupingBy(
                            RDOEquipment::getEquipmentPPUId,
                            Collectors.summingInt(e ->
                                    (int) Optional.ofNullable(e.getCheckers()).orElseGet(List::of).stream()
                                            .filter(c -> Boolean.TRUE.equals(c.operacional()))
                                            .count()
                            )
                    ));

            Map<String, Double> steelCableCounts = platformRdos.stream()
                    .flatMap(r -> Optional.ofNullable(r.getSteelCable()).orElseGet(List::of).stream())
                    .collect(Collectors.groupingBy(
                            SteelCableChecker::lineID,
                            Collectors.summingDouble(c -> Optional.ofNullable(c.quantidade()).orElse(0.0))
                    ));

            Map<String, Double> accessoryKitCounts = platformRdos.stream()
                    .flatMap(r -> Optional.ofNullable(r.getAccessoryKits()).orElseGet(List::of).stream())
                    .collect(Collectors.groupingBy(
                            AccessoryKitDTO::lineID,
                            Collectors.summingDouble(k -> Optional.ofNullable(k.quantidade()).orElse(0.0))
                    ));
            Map<String, Double> pureLines = platformRdos.stream()
                    .flatMap(r -> Optional.ofNullable(r.getLines()).orElseGet(List::of).stream())
                    .collect(Collectors.groupingBy(
                            RDOLine::getParentId,
                            Collectors.summingDouble(k -> Optional.ofNullable(k.getValueMeasured()).orElse(0.0))
                    ));

            BigDecimal totalPlatformValue = BigDecimal.ZERO;
            double totalQuantity = 0.0;

            for (var service : servicesWithoutAvailable) {
                var qtdReal = serviceCounts.getOrDefault(service.getId(), 0.0);

                BigDecimal factor = service.getFactor() != null ? BigDecimal.valueOf(service.getFactor()) : BigDecimal.ONE;
                BigDecimal unitValue = BigDecimal.valueOf(service.getValue()).multiply(factor);
                BigDecimal serviceValue = BMCalculator.calcValue(unitValue, qtdReal);

                totalPlatformValue = totalPlatformValue.add(serviceValue);
                totalQuantity += qtdReal;
            }

            if (hasAvailable && !servicesAvailable.isEmpty()) {
                var availableTotals = calcAvailableTotals(platformRdos, servicesAvailable, availableFactorByName);
                totalPlatformValue = totalPlatformValue.add(availableTotals.totalValue());
                totalQuantity += availableTotals.totalQuantity();
            }

            for (var equip : ppuEquipments) {
                int qtdReal = equipmentCounts.getOrDefault(equip.getId(), 0);
                BigDecimal unitValue = BigDecimal.valueOf(equip.getValue());
                BigDecimal value = BMCalculator.calcValue(unitValue, (double) qtdReal);

                totalPlatformValue = totalPlatformValue.add(value);
                totalQuantity += qtdReal;
            }

            for (var cable : ppuSteelCables) {
                var qtdReal = steelCableCounts.getOrDefault(cable.getId(), 0.0);
                BigDecimal unitValue = BigDecimal.valueOf(cable.getValue());
                BigDecimal value = unitValue.multiply(BigDecimal.valueOf(qtdReal)).setScale(2, RoundingMode.HALF_UP);

                totalPlatformValue = totalPlatformValue.add(value);
                totalQuantity += qtdReal;
            }

            for (var kit : ppuAccessoryKits) {
                double qtdReal = accessoryKitCounts.getOrDefault(kit.getId(), 0.0);
                BigDecimal unitValue = BigDecimal.valueOf(kit.getValue());
                BigDecimal value = BMCalculator.calcValue(unitValue, (double) qtdReal);

                totalPlatformValue = totalPlatformValue.add(value);
                totalQuantity += qtdReal;
            }

            for (var line : ppuPureLines) {
                double qtdReal = accessoryKitCounts.getOrDefault(line.getId(), 0.0);
                BigDecimal unitValue = BigDecimal.valueOf(line.getValue());
                BigDecimal value = BMCalculator.calcValue(unitValue, (double) qtdReal);

                totalPlatformValue = totalPlatformValue.add(value);
                totalQuantity += qtdReal;
            }

            var platformItem = BMPlatformReport.builder()
                    .platform(platform)
                    .totalQuantity(totalQuantity)
                    .type("normal")
                    .totalValue(totalPlatformValue)
                    .build();

            results.add(platformItem);
        }

        BigDecimal grandTotal = results.stream()
                .map(BMPlatformReport::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double grandTotalQuantity = results.stream()
                .mapToDouble(BMPlatformReport::getTotalQuantity)
                .sum();

        var summaryItem = BMPlatformReport.builder()
                .platform(context.getPlatforms().size() + " Plataformas")
                .totalQuantity(grandTotalQuantity)
                .totalValue(grandTotal)
                .type("total")
                .build();

        results.add(summaryItem);

        return results;
    }

    private record AvailableTotals(BigDecimal totalValue, double totalQuantity) {}

    private AvailableTotals calcAvailableTotals(
            List<RDOEntity> platformRdos,
            List<ServiceLine> availableServices,
            Map<String, Double> availableFactorByName
    ) {
        var base = Optional.ofNullable(platformRdos).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .flatMap(r -> Optional.ofNullable(r.getServices()).orElseGet(List::of).stream())
                .filter(Objects::nonNull)
                .filter(s -> s.getServiceID() != null && !s.getServiceID().isBlank())
                .toList();

        Map<String, List<RDOServiceEntity>> byServiceId = base.stream()
                .collect(Collectors.groupingBy(RDOServiceEntity::getServiceID));

        BigDecimal total = BigDecimal.ZERO;
        double quantity = 0.0;

        for (var service : availableServices) {
            var records = byServiceId.getOrDefault(service.getId(), List.of());

            var value = BigDecimal.valueOf(service.getValue());
            var defaultFactor = service.getFactor() != null ? BigDecimal.valueOf(service.getFactor()) : BigDecimal.ONE;

            for (var rec : records) {
                quantity += 1.0;

                BigDecimal factorToUse = defaultFactor;
                var name = rec.getAvailableType();
                if (name != null && !name.isBlank()) {
                    var f = availableFactorByName.get(name);
                    if (f != null) factorToUse = BigDecimal.valueOf(f);
                }

                total = total.add(value.multiply(factorToUse));
            }
        }

        return new AvailableTotals(total.setScale(2, RoundingMode.HALF_UP), quantity);
    }



}