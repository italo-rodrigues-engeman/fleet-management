package com.indux.modules.ppu.domain.strategy;

import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;

import java.util.Map;

public record LineCalculationContext(
        Map<String, ServiceLine> serviceLinesByServiceId,
        Map<String, AvailableType> availableTypesByName
) {}