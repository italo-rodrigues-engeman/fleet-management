package com.indux.modules.ppu.domain.entities.rdo.logger;

import lombok.Getter;

@Getter
public enum RDOLoggerType{
    CREATION("Criação"),
    APPROVAL("Aprovação"),
    REJECTION("Rejeição"),
    EDIT("Edição"),
    COMPETENCE("Competência"),
    CANCELLATION("Cancelamento"),
    BM("Fechamento BM");
    private final String label;

    RDOLoggerType(String label) {
        this.label = label;
    }

}
