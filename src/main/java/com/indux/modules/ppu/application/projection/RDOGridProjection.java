package com.indux.modules.ppu.application.projection;

import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;

import java.time.LocalDate;
import java.util.Map;

public interface RDOGridProjection {
    String getId();

    LocalDate getDate();

    Long getSequentialId();

    String getRegionalNome();

    String getPlatform();

    RDOStatusDP getStatusDP();

    RDOStatusOP getStatusOP();

    String getClientName();

    String getCompetence();

    Map<String, Object> getContract();

    String getCreatorName();
}