package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RDOStatusDP {
    @JsonProperty("Aprovado") APPROVED,
    @JsonProperty("Fechado Competência") CLOSED_COMPETENCE,
    @JsonProperty("Pendente") PENDING,
    @JsonProperty("Correção Operação") CORRECTION_OP,
}
