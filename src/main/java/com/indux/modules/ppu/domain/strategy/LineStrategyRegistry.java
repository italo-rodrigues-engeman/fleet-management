package com.indux.modules.ppu.domain.strategy;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.strategy.line.UniqueValueStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry que mapeia ServiceLine para a LineStrategy apropriada.
 * <p>
 */
@Component
public class LineStrategyRegistry {

    private final Map<String, LineStrategy> strategies;
    private final UniqueValueStrategy defaultStrategy;

    public LineStrategyRegistry(List<LineStrategy> allStrategies, UniqueValueStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;

        this.strategies = allStrategies.stream().collect(Collectors.toMap(strategy -> {
            String name = strategy.getName();
            String strategyName = name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();

            if (strategyName.endsWith("_STRATEGY")) {
                return strategyName.substring(0, strategyName.length() - 9);
            }
            if (strategyName.endsWith("STRATEGY")) {
                return strategyName.substring(0, strategyName.length() - 8);
            }
            return strategyName;
        }, strategy -> strategy, (existing, replacement) -> existing));
    }

    /**
     * Retorna a LineStrategy apropriada para a ServiceLine.
     * <p>
     * Prioridade:
     * 1. Se lineStrategy estiver especificado no ServiceLine, usa essa
     * 2. Caso contrário, usa UniqueValueStrategy (padrão)
     */
    public LineStrategy getStrategy(ServiceLine serviceLine) {
        if (serviceLine == null) {
            return defaultStrategy;
        }

        if (serviceLine.getLineStrategy() != null) {
            String strategyKey = serviceLine.getLineStrategy().getStrategyName();
            LineStrategy strategy = strategies.get(strategyKey);
            if (strategy != null) {
                return strategy;
            }
        }

        return defaultStrategy;
    }
}
