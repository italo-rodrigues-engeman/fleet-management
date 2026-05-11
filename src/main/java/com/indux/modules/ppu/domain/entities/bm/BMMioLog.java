package com.indux.modules.ppu.domain.entities.bm;

import com.indux.modules.ppu.domain.entities.rdo.audit.MioDivergence;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BMMioLog{ 
    RDOLoggerUser user;
    Instant auditAt;
    Boolean success;
    List<MioDivergence> divergences;

}
