package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.response.bm.BMTimeline;
import com.indux.modules.ppu.domain.entities.bm.BMTimelineGeneral;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.bm.BMContext;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import com.indux.modules.ppu.application.services.rdo.helper.RDODurationHelper;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class BMTimelineHandler extends RDODurationHelper {
    private final PPURepository ppuRepository;
    private final RDORepository rdoRepository;
    private final BMMapper mapper;
    private final BMRepository repository;
    private final OvertimeProjectionService overtimeProjectionService;

    public BMTimelineHandler(PPURepository ppuRepository, RDORepository rdoRepository, BMMapper mapper, BMRepository repository, OvertimeProjectionService overtimeProjectionService) {
        this.ppuRepository = ppuRepository;
        this.rdoRepository = rdoRepository;
        this.mapper = mapper;
        this.repository = repository;
        this.overtimeProjectionService = overtimeProjectionService;
    }


    public List<BMTimeline> fetchBMTimeline(String platform, String bmID) {
        var bm = repository.findById(bmID).orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada."));

        DateRange bmPeriod = bm.getPeriod();
        if (bmPeriod == null || bmPeriod.getStart() == null || bmPeriod.getEnd() == null) {
            throw new ModuleNotFoundFailure("BM não possui período de medição definido.");
        }

        BMContext context = new BMContext(bmPeriod.getStart(), bmPeriod.getEnd(), platform, bm.getProjectId());
        var ppu = ppuRepository
                .findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para esse contrato."));

        var platforms = Optional.ofNullable(platform)
                .filter(p -> !"*".equals(p))
                .map(List::of)
                .orElse(ppu.getPlatforms());

        var rdos = rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(
                platforms, RDOStatusOP.APPROVED, bmPeriod.getStart(), bmPeriod.getEnd()
        );
        context.setDaysInQuery((int) ChronoUnit.DAYS.between(bmPeriod.getStart(), bmPeriod.getEnd()) + 1);
        context.setPlatforms(platforms);

        List<BMTimeline> result = new ArrayList<>();

        if (context.isAllPlatforms()) {
            var serviceTimelines = performConsolidatedServiceTimeline(ppu, platforms);
            var equipTimelines = performConsolidatedEquipTimeline(ppu, platforms);
            var steelTimelines = performConsolidatedSteelCableTimeline(ppu, platforms);
            var kitTimelines = performConsolidatedAccessoryKitTimeline(ppu, platforms);
            var pureTimelines = performConsolidatedPureLines(ppu, platforms);

            buildDailyServiceTimeline(serviceTimelines, rdos, context, ppu.getServices());
            buildDailyEquipmentTimeline(equipTimelines, rdos, context);
            buildDailySteelCableTimeline(steelTimelines, rdos, context);
            buildDailyAccessoryKitTimeline(kitTimelines, rdos, context);
            buildDailyPureLines(pureTimelines, rdos, context);

            result.addAll(serviceTimelines);
            result.addAll(equipTimelines);
            result.addAll(steelTimelines);
            result.addAll(kitTimelines);
            result.addAll(pureTimelines);
        } else {
            var serviceTimelines = performBMTimeline(ppu, platform);
            var equipTimelines = performEquipTimeline(ppu, platform);
            var steelTimelines = performSteelCableTimeline(ppu, platform);
            var kitTimelines = performAccessoryKitTimeline(ppu, platform);
            var pureLineTimelines = performPureLineTimeline(ppu, platform);


            buildDailyServiceTimeline(serviceTimelines, rdos, context, ppu.getServices());
            buildDailyEquipmentTimeline(equipTimelines, rdos, context);
            buildDailySteelCableTimeline(steelTimelines, rdos, context);
            buildDailyAccessoryKitTimeline(kitTimelines, rdos, context);
            buildDailyPureLines(pureLineTimelines, rdos, context);


            result.addAll(serviceTimelines);
            result.addAll(equipTimelines);
            result.addAll(steelTimelines);
            result.addAll(kitTimelines);
            result.addAll(pureLineTimelines);
        }

        if (!bm.isFinished()) saveTimelineBM(platform, result, bm);
        return result;
    }


    List<BMTimeline> performBMTimeline(PPUEntity ppu, String platform) {
        return buildTimeline(
                ppu.getServices(),
                platform,
                mapper::toBMTimeline,
                (service, plt) -> resolveQtdExpected(service.getMeasurementForecasts(), plt)
        );
    }

    List<BMTimeline> performEquipTimeline(PPUEntity ppu, String platform) {
        return buildTimeline(
                ppu.getEquipments(),
                platform,
                mapper::toBMTimeline,
                (equip, plt) -> resolveQtdExpected(equip.getMeasurementForecasts(), plt)
        );
    }

    List<BMTimeline> performSteelCableTimeline(PPUEntity ppu, String platform) {
        var steelCables = Optional.ofNullable(ppu.getSteelCables()).orElseGet(List::of);

        if (platform != null && !"*".equals(platform)) {
            steelCables = steelCables.stream()
                    .filter(cable -> cable.getPlatforms() != null && cable.getPlatforms().contains(platform))
                    .toList();
        }

        return buildTimeline(
                steelCables,
                platform,
                mapper::toBMTimeline,
                (cable, plt) -> cable.getTotalPlanned()
        );
    }

    List<BMTimeline> performAccessoryKitTimeline(PPUEntity ppu, String platform) {
        return buildTimeline(
                ppu.getAccessoryKits(),
                platform,
                mapper::toBMTimeline,
                (kit, plt) -> resolveQtdExpected(kit.getMeasurementForecasts(), plt)
        );
    }

    List<BMTimeline> performPureLineTimeline(PPUEntity ppu, String platform) {
        return buildTimeline(
                ppu.getLines(),
                platform,
                mapper::toBMTimeline,
                (line, plt) -> resolveQtdExpected(line.getMeasurementForecasts(), plt)
        );
    }


    private int resolveQtdExpected(List<MeasurementForecast> forecasts, String platform) {
        return Optional.ofNullable(forecasts).orElseGet(List::of).stream()
                .filter(f -> platform.equals(f.getPlatform()))
                .findFirst()
                .map(MeasurementForecast::getTotal)
                .orElse(0);
    }

    private <T> List<BMTimeline> buildTimeline(
            List<T> items,
            String platform,
            BiFunction<T, String, BMTimeline> lineMapper,
            ToIntBiFunction<T, String> qtdResolver
    ) {
        List<BMTimeline> timelines = new ArrayList<>();
        if(items == null || items.isEmpty()) return timelines;
        for (T item : items) {
            BMTimeline line = lineMapper.apply(item, platform);
            int qtdPerDay = qtdResolver.applyAsInt(item, platform);
            line.setQtdExpected(qtdPerDay);
            timelines.add(line);
        }
        return timelines;
    }


    List<BMTimeline> performConsolidatedServiceTimeline(PPUEntity ppu, List<String> platforms) {
        return buildConsolidatedTimeline(
                ppu.getServices(),
                platforms,
                mapper::toBMTimeline,
                (service, plats) -> resolveQtdExpectedForPlatforms(service.getMeasurementForecasts(), plats)
        );
    }

    List<BMTimeline> performConsolidatedEquipTimeline(PPUEntity ppu, List<String> platforms) {
        return buildConsolidatedTimeline(
                ppu.getEquipments(),
                platforms,
                mapper::toBMTimeline,
                (equip, plats) -> resolveQtdExpectedForPlatforms(equip.getMeasurementForecasts(), plats)
        );
    }

    List<BMTimeline> performConsolidatedSteelCableTimeline(PPUEntity ppu, List<String> platforms) {
        return buildConsolidatedTimeline(
                ppu.getSteelCables(),
                platforms,
                mapper::toBMTimeline,
                (cable, plats) -> cable.getTotalPlanned()
        );
    }

    List<BMTimeline> performConsolidatedAccessoryKitTimeline(PPUEntity ppu, List<String> platforms) {
        return buildConsolidatedTimeline(
                ppu.getAccessoryKits(),
                platforms,
                mapper::toBMTimeline,
                (kit, plats) -> resolveQtdExpectedForPlatforms(kit.getMeasurementForecasts(), plats)
        );
    }

    List<BMTimeline> performConsolidatedPureLines(PPUEntity ppu, List<String> platforms) {
        return buildConsolidatedTimeline(
                ppu.getLines(),
                platforms,
                mapper::toBMTimeline,
                (line, plats) -> resolveQtdExpectedForPlatforms(line.getMeasurementForecasts(), plats)
        );
    }



    List<BMTimeline> buildDailyServiceTimeline(
            List<BMTimeline> timelines,
            List<RDOEntity> rdos,
            BMContext context,
            List<ServiceLine> ppuServices
    ) {
        var rdosByDate = rdos.stream().collect(Collectors.groupingBy(RDOEntity::getDate));

        for (int i = 0; i < context.getDaysInQuery(); i++) {
            var currentDate = context.getStart().plusDays(i);
            var currentRdos = rdosByDate.getOrDefault(currentDate, List.of());

            var baseServicesOfDay = context.isAllPlatforms()
                    ? extractServicesFromRdos(currentRdos)
                    : List.<RDOServiceEntity>of();

            var projectedAll = context.isAllPlatforms()
                    ? overtimeProjectionService.project(baseServicesOfDay, ppuServices)
                    : new OvertimeProjectionServiceImpl.ProjectionResult(List.of(), List.of());

            for (var timeline : timelines) {
                List<RDOServiceEntity> baseSlice;

                if (context.isAllPlatforms()) {
                    baseSlice = baseServicesOfDay;
                } else {
                    baseSlice = currentRdos.stream()
                            .filter(entity -> Objects.equals(entity.getPlatform(), timeline.getPlatform()))
                            .flatMap(entity -> Optional.ofNullable(entity.getServices()).orElse(List.of()).stream())
                            .toList();
                }

                OvertimeProjectionServiceImpl.ProjectionResult projectedSlice = context.isAllPlatforms()
                        ? projectedAll
                        : overtimeProjectionService.project(baseSlice, ppuServices);

                var source = Boolean.TRUE.equals(timeline.getIsOvertimeService())
                        ? projectedSlice.virtuals()
                        : projectedSlice.working();

                var value = sumValueMeasuredByServiceId(source, timeline.getId());

                if (timeline.getTimeline() == null) timeline.setTimeline(new ArrayList<>());
                timeline.getTimeline().add(new BMTimeline.DailyEntry(currentDate, currentDate.getDayOfMonth(), value));
            }
        }

        return timelines;
    }

    @NotNull
    private static List<RDOServiceEntity> extractServicesFromRdos(List<RDOEntity> currentRdos) {
        return currentRdos.stream().flatMap(r -> Optional.ofNullable(r.getServices()).orElse(List.of()).stream()).toList();
    }

    private double sumValueMeasuredByServiceId(List<RDOServiceEntity> services, String serviceId) {
        if (services == null || services.isEmpty()) return 0.0;
        return services.stream()
                .filter(Objects::nonNull)
                .filter(s -> Objects.equals(s.getServiceID(), serviceId))
                .map(RDOServiceEntity::getValueMeasured)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    List<BMTimeline> buildDailySteelCableTimeline(
            List<BMTimeline> timelines,
            List<RDOEntity> rdos,
            BMContext context
    ) {
        var rdosByDate = rdos.stream().collect(Collectors.groupingBy(RDOEntity::getDate));

        for (int i = 0; i < context.getDaysInQuery(); i++) {
            LocalDate currentDate = context.getStart().plusDays(i);
            var ros = rdosByDate.getOrDefault(currentDate, List.of());

            for (var timeline : timelines) {
                var cablesOfDay = context.isAllPlatforms()
                        ? ros.stream().flatMap(r -> r.getSteelCable().stream()).toList()
                        : ros.stream()
                        .filter(r -> Objects.equals(r.getPlatform(), timeline.getPlatform()))
                        .flatMap(r -> r.getSteelCable().stream())
                        .toList();

                double value = cablesOfDay.stream()
                        .filter(c -> Objects.equals(c.lineID(), timeline.getId()))
                        .mapToDouble(c -> Optional.ofNullable(c.quantidade()).orElse(0.0))
                        .sum();

                if (timeline.getTimeline() == null) timeline.setTimeline(new ArrayList<>());
                timeline.getTimeline().add(new BMTimeline.DailyEntry(
                        currentDate, currentDate.getDayOfMonth(), value
                ));
            }
        }
        return timelines;
    }

    List<BMTimeline> buildDailyAccessoryKitTimeline(
            List<BMTimeline> timelines,
            List<RDOEntity> rdos,
            BMContext context
    ) {
        var rdosByDate = rdos.stream().collect(Collectors.groupingBy(RDOEntity::getDate));

        for (int i = 0; i < context.getDaysInQuery(); i++) {
            LocalDate currentDate = context.getStart().plusDays(i);
            var ros = rdosByDate.getOrDefault(currentDate, List.of());

            for (var timeline : timelines) {
                var kitsOfDay = context.isAllPlatforms()
                        ? ros.stream().flatMap(r -> r.getAccessoryKits().stream()).toList()
                        : ros.stream()
                        .filter(r -> Objects.equals(r.getPlatform(), timeline.getPlatform()))
                        .flatMap(r -> r.getAccessoryKits().stream())
                        .toList();

                double value = kitsOfDay.stream()
                        .filter(k -> Objects.equals(k.lineID(), timeline.getId()))
                        .mapToDouble(k -> Optional.of(k.quantidade().doubleValue()).orElse(0.0))
                        .sum();

                if (timeline.getTimeline() == null) timeline.setTimeline(new ArrayList<>());
                timeline.getTimeline().add(new BMTimeline.DailyEntry(
                        currentDate, currentDate.getDayOfMonth(), value
                ));
            }
        }
        return timelines;
    }

    List<BMTimeline> buildDailyPureLines(
            List<BMTimeline> timelines,
            List<RDOEntity> rdos,
            BMContext context
    ) {
        var rdosByDate = rdos.stream().collect(Collectors.groupingBy(RDOEntity::getDate));

        for (int i = 0; i < context.getDaysInQuery(); i++) {
            LocalDate currentDate = context.getStart().plusDays(i);
            var ros = rdosByDate.getOrDefault(currentDate, List.of());

            for (var timeline : timelines) {
                var linePerDay = context.isAllPlatforms()
                        ? ros.stream().flatMap(r -> r.getLines().stream()).toList()
                        : ros.stream()
                        .filter(r -> Objects.equals(r.getPlatform(), timeline.getPlatform()))
                        .flatMap(r -> r.getLines().stream())
                        .toList();

                double value = linePerDay.stream()
                        .filter(k -> Objects.equals(k.getParentId(), timeline.getId()))
                        .mapToDouble(k -> Optional.of(k.getValueMeasured()).orElse(0.0))
                        .sum();

                if (timeline.getTimeline() == null) timeline.setTimeline(new ArrayList<>());
                timeline.getTimeline().add(new BMTimeline.DailyEntry(
                        currentDate, currentDate.getDayOfMonth(), value
                ));
            }
        }
        return timelines;
    }

    List<BMTimeline> buildDailyEquipmentTimeline(
            List<BMTimeline> timelines,
            List<RDOEntity> rdos,
            BMContext context
    ) {
        var rdosByDate = rdos.stream().collect(Collectors.groupingBy(RDOEntity::getDate));

        for (int i = 0; i < context.getDaysInQuery(); i++) {
            LocalDate currentDate = context.getStart().plusDays(i);
            var ros = rdosByDate.getOrDefault(currentDate, List.of());

            for (var timeline : timelines) {
                var equipmentsOfDay = context.isAllPlatforms()
                        ? ros.stream()
                        .flatMap(r -> Optional.ofNullable(r.getEquipments()).orElseGet(List::of).stream())
                        .toList()
                        : ros.stream()
                        .filter(r -> Objects.equals(r.getPlatform(), timeline.getPlatform()))
                        .flatMap(r -> Optional.ofNullable(r.getEquipments()).orElseGet(List::of).stream())
                        .toList();

                double value = equipmentsOfDay.stream()
                        .filter(e -> Objects.equals(e.getEquipmentPPUId(), timeline.getId()))
                        .mapToDouble(e ->
                                Optional.ofNullable(e.getCheckers()).orElseGet(List::of).stream()
                                        .filter(c -> Boolean.TRUE.equals(c.operacional()))
                                        .count()
                        )
                        .sum();

                if (timeline.getTimeline() == null) timeline.setTimeline(new ArrayList<>());
                timeline.getTimeline().add(new BMTimeline.DailyEntry(
                        currentDate, currentDate.getDayOfMonth(), value
                ));
            }
        }
        return timelines;
    }

    private void saveTimelineBM(String platform, List<BMTimeline> result, BMEntity bm) {
        var timeline = new BMTimelineGeneral(platform, result);
        var timelineGeneralList = Optional.ofNullable(bm.getTimelines())
                .orElseGet(ArrayList::new);


        var existingIndex = IntStream.range(0, timelineGeneralList.size())
                .filter(i -> timelineGeneralList.get(i).getPlatform().equals(platform))
                .findFirst()
                .orElse(-1);

        if (existingIndex >= 0) {
            timelineGeneralList.set(existingIndex, timeline);
        } else {
            timelineGeneralList.add(timeline);
        }

        bm.setTimelines(timelineGeneralList);
        repository.save(bm);
    }
    private int resolveQtdExpectedForPlatforms(List<MeasurementForecast> forecasts, List<String> platforms) {
        return Optional.ofNullable(forecasts).orElseGet(List::of).stream()
                .filter(f -> platforms.contains(f.getPlatform()))
                .mapToInt(MeasurementForecast::getTotal)
                .sum();
    }

    private <T> List<BMTimeline> buildConsolidatedTimeline(
            List<T> items,
            List<String> platforms,
            BiFunction<T, String, BMTimeline> lineMapper,
            BiFunction<T, List<String>, Integer> qtdResolver
    ) {
        List<BMTimeline> timelines = new ArrayList<>();
        if(items == null || items.isEmpty()) return timelines;
        for (T item : items) {
            BMTimeline line = lineMapper.apply(item, "ALL");
            int qtdPerDay = qtdResolver.apply(item, platforms);
            line.setQtdExpected(qtdPerDay);
            line.setPlatform("ALL");
            timelines.add(line);
        }
        return timelines;
    }

}