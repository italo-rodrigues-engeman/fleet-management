package com.indux.modules.advance_suppliers.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OriginRequest {
    @JsonProperty("PEDIDO") ORDER,
    @JsonProperty("CONTRATO") CONTRACT,
    @JsonProperty("AUTORIZACAO_GESTOR") MANAGER_AUTHORIZATION
}
