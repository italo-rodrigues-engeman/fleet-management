package com.indux.modules.ppu.domain.strategy.time;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyKind;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyPhase;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;

import java.util.EnumSet;
import java.util.List;

/**
 * Strategy para cálculo de hora extra (H.E) e adicional noturno.
 * Define como as horas extras e adicional noturno devem ser tratadas
 * na linha de serviço.
 * 
 * Exemplos:
 * - OvertimeAnotherLineStrategy: valores de hora extra vão para outra linha
 * - OvertimePartitionalStrategy: hora extra vale x% para cada colaborador
 * - GeneralPartitionalStrategy: H.N e H.E valem x% para cada colaborador
 * - MoveOutStrategy: colaborador com hora extra deve ser movido para outra linha
 * - PremiumNigthPartitional: adicional noturno vale x% para cada colaborador
 * - NightPremiumOvertimeToLineStrategy: adicional noturno vale x% e hora extra em outra linha.
 * - SpecialLineStrategy: linha não possui hora extra
 */
public interface TimeStrategy {
    TimeStrategyType type();
    EnumSet<TimeStrategyPhase> phases();
    EnumSet<TimeStrategyKind> kinds();

    default void mutate(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {}

    default List<RDOServiceEntity> produceVirtuals(List<RDOServiceEntity> working, List<ServiceLine> ppuServices) {
        return List.of();
    }
}