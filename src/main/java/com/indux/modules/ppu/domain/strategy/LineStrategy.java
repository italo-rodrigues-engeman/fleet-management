package com.indux.modules.ppu.domain.strategy;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import java.util.List;
import java.util.Map;

/**
 * Strategy para cálculo de quantidade/valor da linha.
 * Define como a quantidade e o valor unitário devem ser calculados
 * independentemente do fator de hora extra ou adicional noturno.
 *
 * Exemplos:
 * - DoubleFoldStrategy: colaborador em dobra vale 2x
 * - HalfStrategy: colaborador na linha vale 1/2
 * - UniqueValueStrategy: colaborador vale 1 (padrão)
 */
public interface LineStrategy {
    String getName();

    RDOServiceEntity calculate(RDOServiceEntity service,
                               List<RDOServiceEntity> allServices,
                               Map<String, ServiceLine> map);

    default RDOServiceEntity calculate(RDOServiceEntity service,
                                       List<RDOServiceEntity> allServices,
                                       LineCalculationContext ctx) {
        return calculate(service, allServices, ctx.serviceLinesByServiceId());
    }
}
