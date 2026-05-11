package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.response.bm.BMServiceReportItem;
import com.indux.modules.ppu.domain.entities.bm.BMContext;
import com.indux.modules.ppu.domain.entities.bm.BMDetailsGeneral;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.item.MeasurementPeriod;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.*;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class BMDetailsHandler {

    private final PPURepository ppuRepository;
    private final RDORepository rdoRepository;
    private final BMRepository bmRepository;
    private final BMMapper mapper;
    private final OvertimeProjectionService overtimeProjectionService;

    public BMDetailsHandler(
            PPURepository ppuRepository,
            RDORepository rdoRepository,
            BMRepository bmRepository,
            BMMapper mapper,
            OvertimeProjectionService overtimeProjectionService) {
        this.ppuRepository = ppuRepository;
        this.rdoRepository = rdoRepository;
        this.bmRepository = bmRepository;
        this.mapper = mapper;
        this.overtimeProjectionService = overtimeProjectionService;
    }

    public List<BMServiceReportItem> fetchItem(String bmID, String platform) {
        var bm = bmRepository.findById(bmID).orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada"));
        var ppu = ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para esse contrato."));

        DateRange bmPeriod = bm.getPeriod();
        if (bmPeriod == null || bmPeriod.getStart() == null || bmPeriod.getEnd() == null) {
            throw new ModuleFailure("BM não possui período de medição definido.");
        }

        LocalDate actualStart = bmPeriod.getStart();
        LocalDate actualEnd = bmPeriod.getEnd();
        LocalDate today = LocalDate.now();

        BMContext context = new BMContext(actualStart, actualEnd, platform, bm.getProjectId());

        LocalDate mStart = getMeasurementStartDate(ppu.getMeasurementPeriod(), actualStart);
        LocalDate mEnd = getMeasurementEndDateFromStart(ppu.getMeasurementPeriod(), mStart);

        context.setMeasurementStartDate(mStart);
        context.setMeasurementEndDate(mEnd);

        LocalDate base = actualStart.isBefore(mStart) ? mStart : actualStart;
        LocalDate ref = actualEnd.isAfter(today) ? today : actualEnd;
        if (ref.isAfter(mEnd)) ref = mEnd;

        context.setCurrentMeasurementDay((int) ChronoUnit.DAYS.between(base, ref) + 1);

        List<RDOEntity> rdos;
        if (context.isAllPlatforms()) {
            List<String> platforms = ppu.getPlatforms();
            rdos = rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                    platforms, RDOStatusOP.APPROVED, actualStart, actualEnd
            );
            context.setPlatforms(platforms);
        } else {
            rdos = rdoRepository.findAllByPlatformAndStatusOPAndDateBetween(
                    platform, RDOStatusOP.APPROVED, actualStart, actualEnd
            );
            context.setPlatforms(List.of(platform));
        }

        var ppuServices = Optional.ofNullable(ppu.getServices()).orElseGet(List::of);
        var ppuEquipments = Optional.ofNullable(ppu.getEquipments()).orElseGet(List::of);
        var allPpuSteelCables = Optional.ofNullable(ppu.getSteelCables()).orElseGet(List::of);
        var ppuSteelCables = context.isAllPlatforms()
                ? allPpuSteelCables
                : allPpuSteelCables.stream()
                .filter(c -> c.getPlatforms() != null && c.getPlatforms().contains(platform))
                .toList();
        var ppuAccessoryKits = Optional.ofNullable(ppu.getAccessoryKits()).orElseGet(List::of);
        var ppuPureLines = Optional.ofNullable(ppu.getLines()).orElseGet(List::of);

        context.setDaysInQuery((int) ChronoUnit.DAYS.between(actualStart, actualEnd) + 1);
        context.setTotalDays(getTotalDaysInFullPeriodFromStart(ppu.getMeasurementPeriod(), context.getMeasurementStartDate()));

        var hasAvailable = ppu.hasAvailableType();

        var servicesWithoutAvailable = hasAvailable
                ? ppuServices.stream().filter(s -> !Boolean.TRUE.equals(s.getDisposicao())).toList()
                : ppuServices;

        var servicesAvailable = hasAvailable
                ? ppuServices.stream().filter(s -> Boolean.TRUE.equals(s.getDisposicao())).toList()
                : List.<ServiceLine>of();

        Map<String, ServiceLine> ppuServiceById = ppuServices.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getId() != null && !s.getId().isBlank())
                .collect(Collectors.toMap(ServiceLine::getId, s -> s, (a, b) -> a));

        Map<String, String> ppuIdByNumber = ppuServices.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getGenericNumber() != null && !s.getGenericNumber().isBlank())
                .filter(s -> s.getId() != null && !s.getId().isBlank())
                .collect(Collectors.toMap(ServiceLine::getGenericNumber, ServiceLine::getId, (a, b) -> a));

        Map<String, Double> availableFactorByName = hasAvailable
                ? Optional.ofNullable(ppu.getAvailableType()).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getName() != null && !t.getName().isBlank())
                .collect(Collectors.toMap(
                        AvailableType::getName,
                        t -> t.getFactor() != null ? t.getFactor() : 1.0,
                        (a, b) -> a
                ))
                : Map.of();

        Set<String> availableServiceIds = hasAvailable
                ? servicesAvailable.stream()
                .map(ServiceLine::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                : Set.of();

        var baseServicesForCalc = collectServicesForCalc(rdos, platform, context.isAllPlatforms());
        var servicesForCalc = overtimeProjectionService.project(baseServicesForCalc, servicesWithoutAvailable);

        Map<String, Double> qtyByServiceId = servicesForCalc.working().stream()
                .filter(Objects::nonNull)
                .map(s -> new AbstractMap.SimpleEntry<>(resolvePpuServiceId(s, ppuServiceById, ppuIdByNumber), s))
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingDouble(e -> Optional.ofNullable(e.getValue().getValueMeasured()).orElse(0.0))
                ));

        Map<String, AvailableReal> availableRealByServiceId = hasAvailable
                ? calcAvailableRealByServiceId(baseServicesForCalc, ppuServiceById, ppuIdByNumber, availableFactorByName, availableServiceIds)
                : Map.of();

        Map<String, Double> equipmentTotals = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getEquipments()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        RDOEquipment::getEquipmentPPUId,
                        Collectors.summingDouble(e ->
                                Optional.ofNullable(e.getCheckers()).orElseGet(List::of).stream()
                                        .filter(c -> Boolean.TRUE.equals(c.operacional()))
                                        .count()
                        )
                ));

        Map<String, Double> steelCableTotals = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getSteelCable()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        SteelCableChecker::lineID,
                        Collectors.summingDouble(c -> Optional.ofNullable(c.quantidade()).orElse(0.0))
                ));

        Map<String, Double> accessoryKitTotals = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getAccessoryKits()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        AccessoryKitDTO::lineID,
                        Collectors.summingDouble(k -> Optional.ofNullable(k.quantidade()).orElse(0.0))
                ));
Map<String, Double> pureLinesTotals = rdos.stream()
                .flatMap(r -> Optional.ofNullable(r.getLines()).orElseGet(List::of).stream())
                .collect(Collectors.groupingBy(
                        RDOLine::getParentId,
                        Collectors.summingDouble(line -> Optional.ofNullable(line.getValueMeasured()).orElse(0.0))
                ));

        List<BMServiceReportItem> result = new ArrayList<>();

        for (var service : ppuServices) {
            if (hasAvailable && Boolean.TRUE.equals(service.getDisposicao())) {
                var real = availableRealByServiceId.getOrDefault(service.getId(), new AvailableReal(0.0, BigDecimal.ZERO));
                result.add(buildAvailableServiceReportItem(service, context, real));
            } else {
                result.add(buildServiceReportItem(service, context, qtyByServiceId));
            }
        }

        for (var equip : ppuEquipments) {
            result.add(buildEquipmentReportItem(equip, context, equipmentTotals));
        }

        for (var cable : ppuSteelCables) {
            result.add(buildSteelCableReportItem(cable, context, steelCableTotals));
        }

        for (var kit : ppuAccessoryKits) {
            result.add(buildAccessoryKitReportItem(kit, context, accessoryKitTotals));
        }
        for (var line : ppuPureLines) {
            result.add(buildLineReportItem(line, context, accessoryKitTotals));
        }

        if (!bm.isFinished()) {
            saveDetailsBM(platform, result, bm);
        }
        return result;
    }

    private String resolvePpuServiceId(
            RDOServiceEntity svc,
            Map<String, ServiceLine> ppuServiceById,
            Map<String, String> ppuIdByNumber
    ) {
        String rawId = normalize(svc.getServiceID());
        if (!rawId.isBlank() && ppuServiceById.containsKey(rawId)) {
            return rawId;
        }

        String num = normalize(svc.getServiceNumber());
        if (num.isBlank()) return null;

        return ppuIdByNumber.get(num);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    private List<RDOServiceEntity> collectServicesForCalc(List<RDOEntity> rdos, String platform, boolean allPlatforms) {
        if (rdos == null || rdos.isEmpty()) return List.of();

        if (allPlatforms) {
            return rdos.stream()
                    .flatMap(r -> Optional.ofNullable(r.getServices()).orElse(List.of()).stream())
                    .toList();
        }

        return rdos.stream()
                .filter(r -> Objects.equals(r.getPlatform(), platform))
                .flatMap(r -> Optional.ofNullable(r.getServices()).orElse(List.of()).stream())
                .toList();
    }

    private LocalDate getMeasurementStartDate(MeasurementPeriod period, LocalDate anchorDate) {
        if (period == null) throw new ModuleFailure("PPU não possui período de medição registrado.");
        int openingDay = period.getOpeningDay();
        if (openingDay < 1 || openingDay > 31) throw new ModuleFailure("Dia de abertura inválido: " + openingDay);
        YearMonth ym = anchorDate.getDayOfMonth() < openingDay ? YearMonth.from(anchorDate).minusMonths(1) : YearMonth.from(anchorDate);
        int day = Math.min(openingDay, ym.lengthOfMonth());
        return LocalDate.of(ym.getYear(), ym.getMonth(), day);
    }

    private LocalDate getMeasurementEndDateFromStart(MeasurementPeriod period, LocalDate cycleStart) {
        if (period == null) throw new ModuleFailure("PPU não possui período de medição registrado.");
        int openingDay = period.getOpeningDay();
        int closingDay = period.getClosingDay();
        YearMonth startYm = YearMonth.from(cycleStart);
        YearMonth endYm = closingDay >= openingDay ? startYm : startYm.plusMonths(1);
        int day = Math.min(closingDay, endYm.lengthOfMonth());
        return LocalDate.of(endYm.getYear(), endYm.getMonth(), day);
    }

    public long getTotalDaysInFullPeriodFromStart(MeasurementPeriod period, LocalDate cycleStart) {
        if (period == null || cycleStart == null) return 0;
        LocalDate cycleEnd = getMeasurementEndDateFromStart(period, cycleStart);
        return ChronoUnit.DAYS.between(cycleStart, cycleEnd) + 1;
    }

    private BMServiceReportItem buildServiceReportItem(ServiceLine service, BMContext context, Map<String, Double> qtyByServiceId) {
        var bm = mapper.toBMServiceReportItem(service);

        int qtdPerDay;
        if (context.isAllPlatforms()) {
            qtdPerDay = Optional.ofNullable(service.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> {
                        List<String> platforms = context.getPlatforms();
                        return platforms != null && platforms.contains(f.getPlatform());
                    })
                    .mapToInt(MeasurementForecast::getTotal)
                    .sum();
        } else {
            qtdPerDay = Optional.ofNullable(service.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> context.getPlatform().equals(f.getPlatform()))
                    .findFirst()
                    .map(MeasurementForecast::getTotal)
                    .orElse(0);
        }

        double qtdExpected = round2((double) qtdPerDay * context.getDaysInQuery());
        double qtdReal = round2(qtyByServiceId.getOrDefault(service.getId(), 0.0));

        bm.setQtdExpected(qtdExpected);
        bm.setQtdReal(qtdReal);

        var factor = service.getFactor() != null ? BigDecimal.valueOf(service.getFactor()) : BigDecimal.ONE;
        var unitValue = BigDecimal.valueOf(service.getValue()).multiply(factor);

        var valueExpected = round2(BMCalculator.calcValue(unitValue, qtdExpected));
        var valueReal = round2(BMCalculator.calcValue(unitValue, qtdReal));

        var expectedDaily = BigDecimal.valueOf(qtdPerDay);

        bm.setValueExpected(valueExpected);
        bm.setValueReal(valueReal);
        if (Boolean.TRUE.equals(service.getIsOvertimeService())) {
            bm.setQtdExpected(0.0);
            bm.setValueExpected(BigDecimal.ZERO);

            bm.setUtilizationPercentage(BigDecimal.ZERO);
            bm.setUtilizationValue(BigDecimal.ZERO);
            bm.setPossibleReach(BigDecimal.ZERO);

            return bm;
        }
        return getBmServiceReportItem(context, bm, unitValue, valueExpected, valueReal, expectedDaily);
    }

    private record AvailableReal(double qtdReal, BigDecimal valueReal) {}

    private Map<String, AvailableReal> calcAvailableRealByServiceId(
            List<RDOServiceEntity> baseServicesForCalc,
            Map<String, ServiceLine> ppuServiceById,
            Map<String, String> ppuIdByNumber,
            Map<String, Double> availableFactorByName,
            Set<String> availableServiceIds
    ) {
        Map<String, Double> qty = new HashMap<>();
        Map<String, BigDecimal> total = new HashMap<>();

        for (var svc : Optional.ofNullable(baseServicesForCalc).orElseGet(List::of)) {
            if (svc == null) continue;

            var ppuId = resolvePpuServiceId(svc, ppuServiceById, ppuIdByNumber);
            if (ppuId == null || ppuId.isBlank()) continue;
            if (!availableServiceIds.contains(ppuId)) continue;

            var line = ppuServiceById.get(ppuId);
            if (line == null) continue;

            qty.merge(ppuId, 1.0, Double::sum);

            BigDecimal value = BigDecimal.valueOf(line.getValue());
            BigDecimal defaultFactor = line.getFactor() != null ? BigDecimal.valueOf(line.getFactor()) : BigDecimal.ONE;

            BigDecimal factorToUse = defaultFactor;
            var name = svc.getAvailableType();
            if (name != null && !name.isBlank()) {
                var f = availableFactorByName.get(name);
                if (f != null) factorToUse = BigDecimal.valueOf(f);
            }

            var add = value.multiply(factorToUse);
            total.merge(ppuId, add, BigDecimal::add);
        }

        Map<String, AvailableReal> out = new HashMap<>();
        for (var entry : qty.entrySet()) {
            var id = entry.getKey();
            out.put(id, new AvailableReal(entry.getValue(), total.getOrDefault(id, BigDecimal.ZERO)));
        }
        return out;
    }

    private BMServiceReportItem buildAvailableServiceReportItem(ServiceLine service, BMContext context, AvailableReal real) {
        var bm = mapper.toBMServiceReportItem(service);

        int qtdPerDay;
        if (context.isAllPlatforms()) {
            qtdPerDay = Optional.ofNullable(service.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> {
                        List<String> platforms = context.getPlatforms();
                        return platforms != null && platforms.contains(f.getPlatform());
                    })
                    .mapToInt(MeasurementForecast::getTotal)
                    .sum();
        } else {
            qtdPerDay = Optional.ofNullable(service.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> context.getPlatform().equals(f.getPlatform()))
                    .findFirst()
                    .map(MeasurementForecast::getTotal)
                    .orElse(0);
        }

        double qtdExpected = (double) qtdPerDay * context.getDaysInQuery();

        bm.setQtdExpected(qtdExpected);
        bm.setQtdReal(real.qtdReal());

        var factor = service.getFactor() != null ? BigDecimal.valueOf(service.getFactor()) : BigDecimal.ONE;
        var unitValue = BigDecimal.valueOf(service.getValue()).multiply(factor);

        var valueExpected = BMCalculator.calcValue(unitValue, qtdExpected);
        var valueReal = real.valueReal();

        bm.setValueExpected(valueExpected);
        bm.setValueReal(valueReal);

        if (Boolean.TRUE.equals(service.getIsOvertimeService())) {
            bm.setQtdExpected(0.0);
            bm.setValueExpected(BigDecimal.ZERO);

            bm.setUtilizationPercentage(null);
            bm.setUtilizationValue(null);
            bm.setPossibleReach(null);

            return bm;
        }

        var expectedDaily = BigDecimal.valueOf(qtdPerDay);
        return getBmServiceReportItem(context, bm, unitValue, valueExpected, valueReal, expectedDaily);
    }

    @NotNull
    private BMServiceReportItem getBmServiceReportItem(BMContext context, BMServiceReportItem bm, BigDecimal unitValue, BigDecimal valueExpected, BigDecimal valueReal, BigDecimal expectedDaily) {
        var utilizationPct = round2(BMCalculator.calcAproveitamentoPorcentagem(
                valueReal, expectedDaily, context.getCurrentMeasurementDay(), unitValue
        ));
        bm.setUtilizationPercentage(utilizationPct);
        bm.setUtilizationValue(round2(BMCalculator.calcAproveitamentoEmReal(valueExpected, valueReal)));

        var possibleReach = round2(BMCalculator.calcPossibleReach(
                valueReal,
                context.getEnd(),
                context.getMeasurementStartDate(),
                context.getMeasurementEndDate()
        ));
        bm.setPossibleReach(possibleReach);

        return bm;
    }

    private BMServiceReportItem buildEquipmentReportItem(EquipmentLine equip, BMContext context, Map<String, Double> equipmentTotals) {
        var bm = mapper.toBMServiceReportItem(equip);

        int qtdPerDay;
        if (context.isAllPlatforms()) {
            qtdPerDay = Optional.ofNullable(equip.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> {
                        List<String> platforms = context.getPlatforms();
                        return platforms != null && platforms.contains(f.getPlatform());
                    })
                    .mapToInt(MeasurementForecast::getTotal)
                    .sum();
        } else {
            qtdPerDay = Optional.ofNullable(equip.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> context.getPlatform().equals(f.getPlatform()))
                    .findFirst()
                    .map(MeasurementForecast::getTotal)
                    .orElse(0);
        }

        double qtdExpected = round2((double) qtdPerDay * context.getDaysInQuery());
        double qtdReal = round2(equipmentTotals.getOrDefault(equip.getId(), 0.0));

        bm.setQtdExpected(qtdExpected);
        bm.setQtdReal(qtdReal);

        BigDecimal unitValue = BigDecimal.valueOf(equip.getValue());
        BigDecimal valueExpected = round2(BMCalculator.calcValue(unitValue, qtdExpected));
        BigDecimal valueReal = round2(BMCalculator.calcValue(unitValue, qtdReal));

        BigDecimal expectedDaily = BigDecimal.valueOf(qtdPerDay);

        bm.setValueExpected(valueExpected);
        bm.setValueReal(valueReal);

        return getBmServiceReportItem(context, bm, unitValue, valueExpected, valueReal, expectedDaily);
    }

    private BMServiceReportItem buildSteelCableReportItem(SteelCableLine cable, BMContext context, Map<String, Double> steelCableTotals) {
        var bm = mapper.toBMServiceReportItem(cable);

        int qtdPerDay = cable.getTotalPlanned() != null ? cable.getTotalPlanned() : 0;

        double qtdExpected = round2((double) qtdPerDay * context.getDaysInQuery());
        double qtdReal = round2(steelCableTotals.getOrDefault(cable.getId(), 0.0));

        bm.setQtdExpected(qtdExpected);
        bm.setQtdReal(qtdReal);

        BigDecimal unitValue = BigDecimal.valueOf(cable.getValue());
        BigDecimal valueExpected = round2(BMCalculator.calcValue(unitValue, qtdExpected));
        BigDecimal valueReal = round2(BMCalculator.calcValue(unitValue, qtdReal));

        BigDecimal expectedDaily = BigDecimal.valueOf(qtdPerDay);

        bm.setValueExpected(valueExpected);
        bm.setValueReal(valueReal);

        return getBmServiceReportItem(context, bm, unitValue, valueExpected, valueReal, expectedDaily);
    }

    private BMServiceReportItem buildAccessoryKitReportItem(AccessoryKitLine kit, BMContext context, Map<String, Double> accessoryKitTotals) {
        var bm = mapper.toBMServiceReportItem(kit);

        int qtdPerDay;
        if (context.isAllPlatforms()) {
            qtdPerDay = Optional.ofNullable(kit.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> {
                        List<String> platforms = context.getPlatforms();
                        return platforms != null && platforms.contains(f.getPlatform());
                    })
                    .mapToInt(MeasurementForecast::getTotal)
                    .sum();
        } else {
            qtdPerDay = Optional.ofNullable(kit.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> context.getPlatform().equals(f.getPlatform()))
                    .findFirst()
                    .map(MeasurementForecast::getTotal)
                    .orElse(0);
        }

        double qtdExpected = round2((double) qtdPerDay * context.getDaysInQuery());
        double qtdReal = round2(accessoryKitTotals.getOrDefault(kit.getId(), 0.0));

        bm.setQtdExpected(qtdExpected);
        bm.setQtdReal(qtdReal);

        BigDecimal unitValue = BigDecimal.valueOf(kit.getValue());
        BigDecimal valueExpected = round2(BMCalculator.calcValue(unitValue, qtdExpected));
        BigDecimal valueReal = round2(BMCalculator.calcValue(unitValue, qtdReal));

        BigDecimal expectedDaily = BigDecimal.valueOf(qtdPerDay);

        bm.setValueExpected(valueExpected);
        bm.setValueReal(valueReal);

        return getBmServiceReportItem(context, bm, unitValue, valueExpected, valueReal, expectedDaily);
    }
    private BMServiceReportItem buildLineReportItem(LinePPU line, BMContext context, Map<String, Double> pureLineTotals) {
        var bm = mapper.toBMServiceReportItem(line);

        int qtdPerDay;
        if (context.isAllPlatforms()) {
            qtdPerDay = Optional.ofNullable(line.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> {
                        List<String> platforms = context.getPlatforms();
                        return platforms != null && platforms.contains(f.getPlatform());
                    })
                    .mapToInt(MeasurementForecast::getTotal)
                    .sum();
        } else {
            qtdPerDay = Optional.ofNullable(line.getMeasurementForecasts()).orElseGet(List::of).stream()
                    .filter(f -> context.getPlatform().equals(f.getPlatform()))
                    .findFirst()
                    .map(MeasurementForecast::getTotal)
                    .orElse(0);
        }

        double qtdExpected = round2((double) qtdPerDay * context.getDaysInQuery());
        double qtdReal = round2(pureLineTotals.getOrDefault(line.getId(), 0.0));

        bm.setQtdExpected(qtdExpected);
        bm.setQtdReal(qtdReal);

        BigDecimal unitValue = BigDecimal.valueOf(line.getValue());
        BigDecimal valueExpected = round2(BMCalculator.calcValue(unitValue, qtdExpected));
        BigDecimal valueReal = round2(BMCalculator.calcValue(unitValue, qtdReal));

        BigDecimal expectedDaily = BigDecimal.valueOf(qtdPerDay);

        bm.setValueExpected(valueExpected);
        bm.setValueReal(valueReal);

        return getBmServiceReportItem(context, bm, unitValue, valueExpected, valueReal, expectedDaily);
    }

    private void saveDetailsBM(String platform, List<BMServiceReportItem> result, BMEntity bm) {
        var notFound = -1;
        var details = new BMDetailsGeneral(platform, result);
        var detailsList = Optional.ofNullable(bm.getDetails()).orElseGet(ArrayList::new);

        var existingIndex = IntStream.range(0, detailsList.size())
                .filter(i -> detailsList.get(i).getPlatform().equals(platform))
                .findFirst()
                .orElse(notFound);

        if (existingIndex >= 0) {
            detailsList.set(existingIndex, details);
        } else {
            detailsList.add(details);
        }

        bm.setDetails(detailsList);
        bmRepository.save(bm);
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal round2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
