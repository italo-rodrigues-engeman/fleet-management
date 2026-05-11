package com.indux.modules.modulo_mega.domain.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ItemSolicitationStatus {

    @JsonProperty("Rejeitado")
    REJECTED,

    @JsonProperty("Finalizado")
    REGISTERED,
    
    @JsonProperty("Cadastro")
    REGISTRATION,
    
    @JsonProperty("Validação técnica")
    TECHNICAL_VALIDATION,
    
    @JsonProperty("Validação Tributária")
    TAX_VALIDATION
}

