package com.indux.modules.ppu.domain.strategy.line;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.LineStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Strategy padrão: o colaborador adicionado nessa linha deve valer 1.
 * Quantidade e valor permanecem inalterados.
 */
@Component
public class UniqueValueStrategy implements LineStrategy {
    @Override
    public RDOServiceEntity calculate(RDOServiceEntity service, List<RDOServiceEntity> allServices,
            Map<String, ServiceLine> map) {
        if (service == null)
            return null;
        service.setValueMeasured(BigDecimal.ONE.doubleValue());
        return service;
    }

    @Override
    public String getName() {
        return "UniqueValueStrategy";
    }

}
