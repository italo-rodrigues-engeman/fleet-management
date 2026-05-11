package com.indux.modules.ppu.application.projection;

import java.time.LocalDate;

public interface PendingRDOProjection {
    String    getId();
    Long      getSequentialId();
    LocalDate getDate();
    Long getPpuId();
}
