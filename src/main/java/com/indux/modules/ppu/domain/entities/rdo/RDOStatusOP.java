package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RDOStatusOP {
    @JsonProperty("Aprovado") APPROVED,
    @JsonProperty("Correção DP") CORRECTION,
    @JsonProperty("Pendente") PENDING,
    @JsonProperty("Fechado BM") BM,
}
