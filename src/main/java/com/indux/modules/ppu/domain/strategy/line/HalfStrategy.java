package com.indux.modules.ppu.domain.strategy.line;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.LineStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Strategy para colaborador que vale metade: o colaborador que estiver nessa linha vale 1/2.
 * A quantidade efetiva é dividida por 2.
 */
@Component
public class HalfStrategy implements LineStrategy {
    @Override
    public RDOServiceEntity calculate(RDOServiceEntity service, List<RDOServiceEntity> allServices,
            Map<String, ServiceLine> map) {
        if (service == null)
            return null;
        service.setValueMeasured(0.5);
        return service;
    }

    @Override
    public String getName() {
        return "HalfStrategy";
    }

}
