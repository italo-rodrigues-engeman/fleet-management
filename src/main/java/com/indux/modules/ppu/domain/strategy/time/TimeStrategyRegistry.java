package com.indux.modules.ppu.domain.strategy.time;

import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry que mapeia ServiceLine para a TimeStrategy apropriada.
 * 
 * Por enquanto, usa regras baseadas nas propriedades da linha.
 * No futuro, pode ser configurável por contrato através de banco de dados
 * ou configuração externa.
 */
@Component
public class TimeStrategyRegistry {
    
    private final TimeStrategy defaultStrategy;
    private final Map<TimeStrategyType, TimeStrategy> strategies;

    public TimeStrategyRegistry(List<TimeStrategy> allStrategies, TimeStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;

        var map = new EnumMap<TimeStrategyType, TimeStrategy>(TimeStrategyType.class);

        for (var strategy : allStrategies) {
            if (strategy == null) continue;
            var type = strategy.type();
            if (type == null) {
                throw new IllegalStateException("TimeStrategy com type() = null: " + strategy.getClass().getName());
            }
            map.putIfAbsent(type, strategy);
        }

        this.strategies = map;
    }
}

