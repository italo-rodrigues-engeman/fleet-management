package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.form.DocumentStatus;

import java.util.Map;

public interface PPUEnhancedGridProjection {
    
    String getId();
    
    @JsonProperty("codigo") 
    Long getCodeID();
    
    @JsonProperty("apelido") 
    String getNickname();
    
    @JsonProperty("regional") 
    Long getRegionalId();
    
    @JsonProperty("regionalNome") 
    String getRegionalNome();
    
    @JsonProperty("clientId") 
    Long getClientId();
    
    @JsonProperty("clienteNome") 
    String getClientName();
    
    @JsonProperty("contrato") 
    Map<String, Object> getContract();
    
    @JsonProperty("contratoNome") 
    String getContractName();
    
    @JsonProperty("status") 
    DocumentStatus getStatus();
    
    @JsonProperty("responsavel") 
    String getResponsible();

    @JsonProperty("projetoId")
    Long getProjectId();

}