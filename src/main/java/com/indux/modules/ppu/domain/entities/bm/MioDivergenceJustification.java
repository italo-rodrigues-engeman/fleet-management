package com.indux.modules.ppu.domain.entities.bm;

import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Justificativa persistida para uma divergência MIO.
 * Vive separada do {@code BMMioLog} para não ser sobrescrita
 * quando uma nova auditoria MIO é executada.
 *
 * <p>
 * A combinação de {@code registration} + {@code date} identifica
 * unicamente a divergência que está sendo justificada.
 * </p>
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MioDivergenceJustification {
    String registration;
    LocalDate date;
    String justification;
    Instant justifiedAt;
    RDOLoggerUser justifiedBy;
}
