package com.indux.modules.ppu.domain.strategy.time.impl;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.TimeStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aplica a regra de HORA EXTRA particional diretamente na própria linha (não gera linha virtual).
 *
 * Regra do contrato:
 * - A hora extra aumenta o "valueMeasured" da linha por meio de um fator.
 * - Incremento = (horasExtras / 12)
 * - Fator = 1 + incremento
 * - O incremento é truncado em 3 casas decimais (ex.: 1h => 0,083; fator => 1,083).
 *
 * Exemplo:
 * - 1 hora extra: incremento = 1/12 = 0,0833... => 0,083; fator = 1,083
 * - Se basePresence = 1, então valueMeasured final = 1 * 1,083 = 1,083
 *
 * Observações:
 * - Não deve ser aplicada em serviços "à disposição".
 * - Estratégia do tipo MUTATE: recalcula o valor medido no fluxo de criação/edição do RDO.
 * - Idempotente: sempre recalcula a partir do "basePresence" (present/generated), não do valueMeasured atual.
 */
@Component
public class OvertimePartitionalStrategy implements TimeStrategy {

    @Override
    public TimeStrategyType type() {
        return TimeStrategyType.OVERTIME_PARTITIONAL;
    }

    @Override
    public EnumSet<TimeStrategyPhase> phases() {
        return EnumSet.of(TimeStrategyPhase.CREATE, TimeStrategyPhase.PROJECTION);
    }

    @Override
    public EnumSet<TimeStrategyKind> kinds() {
        return EnumSet.of(TimeStrategyKind.MUTATE);
    }

    @Override
    public void mutate(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        if (working == null || working.isEmpty() || ppuServices == null || ppuServices.isEmpty()) return;

        var lineById = ppuServices.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        for (var svc : working) {
            if (svc == null) continue;

            if (Boolean.TRUE.equals(svc.getDisposicao())) continue;

            var line = lineById.get(svc.getServiceID());
            if (line == null) continue;

            if (line.getTimeStrategy() != type()) continue;

            var overtime = Optional.ofNullable(svc.getOvertimeHourTotais()).orElse(Duration.ZERO);
            if (overtime.isZero()) continue;

            double hours = overtime.toSeconds() / 3600.0;

            double incrementRaw = hours / 12.0;
            double increment = Math.floor(incrementRaw * 1000.0) / 1000.0;
            double factor = 1.0 + increment;
            if (factor <= 0.0) continue;

            boolean generated = Boolean.TRUE.equals(svc.getGenerated());
            double basePresence = generated ? 0.0 : (svc.isPresent() ? 1.0 : 0.0);

            svc.setValueMeasured(basePresence * factor);
        }
    }
}
