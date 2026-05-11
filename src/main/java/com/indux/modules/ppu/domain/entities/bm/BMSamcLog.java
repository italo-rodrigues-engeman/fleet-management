package com.indux.modules.ppu.domain.entities.bm;

import com.indux.modules.ppu.domain.entities.rdo.audit.AuditDivergence;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BMSamcLog {
    RDOLoggerUser user;
    Instant auditAt;
    Boolean success;
    List<AuditDivergence> divergences;
}

