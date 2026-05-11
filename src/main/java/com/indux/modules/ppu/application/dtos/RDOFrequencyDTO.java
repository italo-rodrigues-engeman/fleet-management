package com.indux.modules.ppu.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.RDOFrequency;

public enum RDOFrequencyDTO {
    @JsonProperty("DIARIA")
    DAILY,
    @JsonProperty("SEMANAL")
    WEEKLY,
    @JsonProperty("MENSAL")
    MONTHLY,
    @JsonProperty("SOB_DEMANDA")
    ON_DEMAND;

    public static RDOFrequencyDTO fromDomain(RDOFrequency frequency) {
        if (frequency == null) return null;
        return switch (frequency) {
            case DAILY -> DAILY;
            case WEEKLY -> WEEKLY;
            case MONTHLY -> MONTHLY;
            case ON_DEMAND -> ON_DEMAND;
        };
    }

    public static RDOFrequency toDomain(RDOFrequencyDTO dto) {
        if (dto == null) return null;
        return switch (dto) {
            case DAILY -> RDOFrequency.DAILY;
            case WEEKLY -> RDOFrequency.WEEKLY;
            case MONTHLY -> RDOFrequency.MONTHLY;
            case ON_DEMAND -> RDOFrequency.ON_DEMAND;
        };
    }
}

