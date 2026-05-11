package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.form.DocumentStatus;

import java.util.Map;

public interface PPUProjection {
    String getId();
    @JsonProperty("codigo") Long getCodeID();
    @JsonProperty("apelido") String getNickname();
    @JsonProperty("regional") Long getRegionalId();
    @JsonProperty("clientId") Long getClientId();
    @JsonProperty("contrato") Map<String, Object> getContract();
    @JsonProperty("status") DocumentStatus getStatus();
    @JsonProperty("projetoId") Long getProjectId();
}
