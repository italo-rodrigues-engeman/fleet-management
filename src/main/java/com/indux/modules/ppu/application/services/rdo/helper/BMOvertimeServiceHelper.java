package com.indux.modules.ppu.application.services.rdo.helper;

import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;

import java.util.*;
import java.util.stream.Collectors;

public class BMOvertimeServiceHelper {

    public static Map<String, Double> calculateServiceQuantities(
            List<RDOEntity> rdos,
            List<ServiceLine> ppuServices,
            OvertimeProjectionService projectionService
    ) {
        var base = Optional.ofNullable(rdos).orElseGet(List::of).stream()
                .flatMap(r -> Optional.ofNullable(r.getServices()).orElseGet(List::of).stream())
                .filter(Objects::nonNull)
                .toList();

        var projected = projectionService.project(base, Optional.ofNullable(ppuServices).orElseGet(List::of));

        return projected.working().stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getServiceID() != null && !s.getServiceID().isBlank())
                .collect(Collectors.groupingBy(
                        RDOServiceEntity::getServiceID,
                        Collectors.summingDouble(s -> {
                            var v = s.getValueMeasured();
                            return v != null ? v : 1.0;
                        })
                ));
    }
}