package com.indux.modules.ppu.domain.strategy.time.impl;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NightPremiumAndOvertimeToLineStrategy implements TimeStrategy {

    @Override
    public TimeStrategyType type() {
        return TimeStrategyType.NIGHT_PREMIUM_OVERTIME_TO_LINE;
    }

    @Override
    public EnumSet<TimeStrategyPhase> phases() {
        return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION);
    }

    @Override
    public EnumSet<TimeStrategyKind> kinds() {
        return EnumSet.of(TimeStrategyKind.MUTATE, TimeStrategyKind.PRODUCE_VIRTUAL);
    }

    @Override
    public void mutate(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        if (working == null || working.isEmpty() || ppuServices == null || ppuServices.isEmpty()) return;

        var lineById = ppuServices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        for (var svc : working) {
            if (svc == null) continue;

            var line = lineById.get(svc.getServiceID());
            if (line == null) continue;

            if (line.getTimeStrategy() != type()) continue;

            var night = Optional.ofNullable(svc.getNightShiftPremium()).orElse(Duration.ZERO);
            if (night.isZero()) continue;

            double qhn = night.toSeconds() / 3600.0;

            double factorRaw = ((qhn * 1.2) + (12.0 - qhn)) / 12.0;
            double factor = Math.floor(factorRaw * 100.0) / 100.0; // truncar após 2 casas

            double base = Optional.ofNullable(svc.getValueMeasured()).orElse(0.0);
            if (base <= 0.0) base = 1.0;

            svc.setValueMeasured(base * factor);
        }

    }

    @Override
    public List<RDOServiceEntity> produceVirtuals(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        if (working == null || working.isEmpty() || ppuServices == null || ppuServices.isEmpty()) return List.of();

        var lineById = ppuServices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        var totals = new HashMap<String, Duration>();

        for (var svc : working) {
            if (svc == null) continue;

            var line = lineById.get(svc.getServiceID());
            if (line == null) continue;

            if (line.getTimeStrategy() != type()) continue;

            if (Boolean.TRUE.equals(line.getIsOvertimeService())) continue;

            var overtimeId = line.getOvertimeService();
            if (overtimeId == null || overtimeId.isBlank()) continue;

            var d = Optional.ofNullable(svc.getOvertimeHourTotais()).orElse(Duration.ZERO);
            if (d.isZero()) continue;

            totals.merge(overtimeId, d, Duration::plus);
        }

        if (totals.isEmpty()) return List.of();

        return totals.entrySet().stream()
                .map(e -> buildOvertimeVirtual(e.getKey(), e.getValue(), lineById))
                .toList();
    }

    private RDOServiceEntity buildOvertimeVirtual(String overtimeId, Duration total, Map<String, ServiceLine> lineById) {
        var overtimeLine = lineById.get(overtimeId);
        if (overtimeLine == null) {
            throw new ModuleFailure("Serviço de HORA EXTRA de id '" + overtimeId + "' não foi encontrado na PPU.");
        }

        var hours = total.toSeconds() / 3600.0;

        var rdo = new RDOServiceEntity();
        rdo.setServiceID(overtimeId);
        rdo.setServiceName(overtimeLine.getName());
        rdo.setServiceNumber(overtimeLine.getGenericNumber());
        rdo.setOvertimeHourTotais(total);
        rdo.setValueMeasured(hours);
        return rdo;
    }
}