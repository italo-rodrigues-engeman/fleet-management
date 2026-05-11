package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RDORejectionType {
    @JsonProperty("turno_divergente")
    SHIFT_DIVERGENCE,

    @JsonProperty("colaborador_nao_embarcado")
    EMPLOYEE_NOT_BOARDED,

    @JsonProperty("horario_prolongado")
    LONG_WORKING_HOURS,

    @JsonProperty("plataforma_errada")
    WRONG_PLATFORM,

    @JsonProperty("mio_programacao")
    MIO_OPEN_SCHEDULE,

    @JsonProperty("outros")
    OTHER
}
