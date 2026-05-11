package com.indux.modules.ppu.domain.strategy;

import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.types.LineStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LineStrategiesApplier {

    private final LineStrategyRegistry lineStrategyRegistry;

    public void apply(RDOEntity entity, PPUEntity ppu) {
        if (entity.getServices() == null || entity.getServices().isEmpty()) return;

        Map<String, ServiceLine> serviceLineMap = Optional.ofNullable(ppu.getServices())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        Map<String, AvailableType> availableTypeByName = Optional.ofNullable(ppu.getAvailableType())
                .orElse(List.of())
                .stream()
                .filter(a -> a.getName() != null && !a.getName().isBlank())
                .collect(Collectors.toMap(AvailableType::getName, Function.identity(), (a, b) -> a));

        ensureParentLinesExist(entity, serviceLineMap);

        var ctx = new LineCalculationContext(serviceLineMap, availableTypeByName);

        entity.getServices().forEach(service -> {
            ServiceLine serviceLine = serviceLineMap.get(service.getServiceID());
            if (serviceLine == null) return;
            LineStrategy strategy = lineStrategyRegistry.getStrategy(serviceLine);
            strategy.calculate(service, entity.getServices(), ctx);
        });
    }

    private void ensureParentLinesExist(RDOEntity entity, Map<String, ServiceLine> serviceLineMap) {
        List<RDOServiceEntity> services = entity.getServices();
        if (services == null || services.isEmpty()) return;

        Set<String> existingIds = services.stream()
                .map(RDOServiceEntity::getServiceID)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<RDOServiceEntity> toPrepend = new ArrayList<>();

        for (RDOServiceEntity s : services) {
            if (!Boolean.TRUE.equals(s.getDisposicao())) continue;

            ServiceLine childLine = serviceLineMap.get(s.getServiceID());
            if (childLine == null) continue;

            String parentId = childLine.getParentId();
            if (parentId == null || parentId.isBlank()) continue;

            if (existingIds.contains(parentId)) continue;

            ServiceLine parentLine = serviceLineMap.get(parentId);
            if (parentLine == null) continue;

            if (parentLine.getLineStrategy() != LineStrategyType.AVAILABLE_REDISTRIBUTION) continue;

            RDOServiceEntity ghost = buildGhostParentService(parentLine, s);
            toPrepend.add(ghost);
            existingIds.add(parentId);

            String next = parentLine.getParentId();
            while (next != null && !next.isBlank() && !existingIds.contains(next)) {
                ServiceLine up = serviceLineMap.get(next);
                if (up == null) break;
                RDOServiceEntity upGhost = buildGhostParentService(up, s);
                toPrepend.add(upGhost);
                existingIds.add(next);
                next = up.getParentId();
            }
        }

        if (!toPrepend.isEmpty()) {
            services.addAll(0, toPrepend);
        }
    }

    private RDOServiceEntity buildGhostParentService(ServiceLine parentLine, RDOServiceEntity childSample) {
        RDOServiceEntity ghost = new RDOServiceEntity();

        ghost.setServiceID(parentLine.getId());
        ghost.setServiceName(parentLine.getName());
        ghost.setServiceNumber(parentLine.getGenericNumber());
        ghost.setDisposicao(false);
        ghost.setPresent(false);
        ghost.setOvertimes(List.of());
        ghost.setSispat("");
        ghost.setTeamLeader(false);
        ghost.setFlagman(false);
        ghost.setDayType(childSample.getDayType());
        ghost.setNightShiftPremium(Duration.ZERO);
        ghost.setNormalHours(Duration.ZERO);
        ghost.setHourTotais(Duration.ZERO);
        ghost.setOvertimeHourTotais(Duration.ZERO);
        ghost.setDoubleFold(false);
        ghost.setValueMeasured(0.0);
        ghost.setAvailableType(null);
        ghost.setGenerated(true);
        return ghost;
    }
}