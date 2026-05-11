package com.indux.modules.ppu.domain.strategy.available;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.strategy.LineCalculationContext;
import com.indux.modules.ppu.domain.strategy.LineStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Strategy para redistribuição de valor de "A disposição".
 * <p>
 * Regras:
 * 1. Se o serviço for "disposição" (isDisposicao() == true), seu valor medido
 * será 0.
 * 2. Se for um serviço pai (não disposição), ele verifica se possui filhos "a
 * disposição".
 * Para cada filho "a disposição" encontrado, adiciona 0.7 ao valor medido do
 * pai.
 * <p>
 * A atribuição ao pai ocorre apenas na primeira ocorrência do serviço pai na
 * lista,
 * para evitar duplicidade de soma se houver múltiplos colaboradores no mesmo
 * serviço.
 */
@Component
public class AvailableRedistributionStrategy implements LineStrategy {

    @Override
    public String getName() {
        return "AvailableRedistributionStrategy";
    }

    @Override
    public RDOServiceEntity calculate(RDOServiceEntity service,
                                      List<RDOServiceEntity> allServices,
                                      Map<String, ServiceLine> map) {
        return calculate(service, allServices, new LineCalculationContext(map, Map.of()));
    }

    @Override
    public RDOServiceEntity calculate(RDOServiceEntity service,
                                      List<RDOServiceEntity> allServices,
                                      LineCalculationContext ctx) {
        if (service == null) return null;

        if (Boolean.TRUE.equals(service.getDisposicao())) {
            service.setValueMeasured(0.0);
            return service;
        }

        double basePresence = resolveBaseValue(service);

        double currentMeasured = 0.0;
        if (service.getValueMeasured() != null) {
            currentMeasured = service.getValueMeasured();
        }

        double timeFactor = 1.0;
        if (basePresence > 0.0 && currentMeasured > 0.0) {
            timeFactor = currentMeasured / basePresence;
        }

        double measured = basePresence * timeFactor;
        service.setValueMeasured(measured);

        ServiceLine currentLine = ctx.serviceLinesByServiceId().get(service.getServiceID());
        if (currentLine == null) return service;

        if (isFirstOccurrence(service, allServices)) {
            double additional = calculateAdditionalValue(currentLine, allServices, ctx);
            service.setValueMeasured(measured + additional);
        }

        return service;
    }

    private double resolveBaseValue(RDOServiceEntity service) {
        boolean present = service.isPresent();
        boolean generated = Boolean.TRUE.equals(service.getGenerated());

        if (generated) return 0.0;
        return present ? 1.0 : 0.0;
    }

    private boolean isFirstOccurrence(RDOServiceEntity target, List<RDOServiceEntity> allServices) {
        for (RDOServiceEntity s : allServices) {
            if (Objects.equals(s.getServiceID(), target.getServiceID())) {
                return s == target;
            }
        }
        return false;
    }

    private double calculateAdditionalValue(ServiceLine currentLine,
                                            List<RDOServiceEntity> allServices,
                                            LineCalculationContext ctx) {
        double sum = 0.0;

        for (RDOServiceEntity childService : allServices) {
            if (!Boolean.TRUE.equals(childService.getDisposicao())) continue;

            ServiceLine childLine = ctx.serviceLinesByServiceId().get(childService.getServiceID());
            if (childLine == null) continue;

            if (!Objects.equals(childLine.getParentId(), currentLine.getId())) continue;

            sum += resolveAvailableFactor(childService, childLine, ctx.availableTypesByName());
        }

        return sum;
    }

    private double resolveAvailableFactor(RDOServiceEntity service,
                                          ServiceLine line,
                                          Map<String, AvailableType> availableTypesByName) {
        String typeName = service.getAvailableType();
        if (typeName != null && !typeName.isBlank()) {
            AvailableType t = availableTypesByName.get(typeName);
            if (t != null && t.getFactor() != null) {
                return t.getFactor();
            }
        }

        Double factor = line.getFactor();
        return factor != null ? factor : 0.0;
    }
}
