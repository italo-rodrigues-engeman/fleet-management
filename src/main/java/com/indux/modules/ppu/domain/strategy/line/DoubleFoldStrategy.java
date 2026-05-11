package com.indux.modules.ppu.domain.strategy.line;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.LineStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Strategy para colaborador em dobra: o colaborador que está em dobra deve valer 2x.
 * A quantidade efetiva é multiplicada por 2.
 */
@Component
public class DoubleFoldStrategy implements LineStrategy {

    @Override
    public String getName() {
        return "DoubleFoldStrategy";
    }

    @Override
    public RDOServiceEntity calculate(RDOServiceEntity service, List<RDOServiceEntity> allServices,
            Map<String, ServiceLine> map) {
        if (service == null)
            return null;
        if (service.getDoubleFold()) {
            service.setValueMeasured(BigDecimal.TWO.doubleValue());
        } else {
            service.setValueMeasured(BigDecimal.ONE.doubleValue());
        }
        return service;
    }
}
