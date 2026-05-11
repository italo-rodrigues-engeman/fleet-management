package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PPUEnhancedGridProjectionImpl implements PPUEnhancedGridProjection {
    
    private String id;
    
    @JsonProperty("codigo")
    private Long codeID;
    
    @JsonProperty("apelido")
    private String nickname;
    
    @JsonProperty("regional")
    private Long regionalId;
    
    @JsonProperty("regionalNome")
    private String regionalNome;
    
    @JsonProperty("clientId")
    private Long clientId;
    
    @JsonProperty("clienteNome")
    private String clientName;
    
    @JsonProperty("contrato")
    private Map<String, Object> contract;
    
    @JsonProperty("contratoNome")
    private String contractName;
    
    @JsonProperty("status")
    private DocumentStatus status;

    @JsonProperty("responsavel")
    private String responsible;

    @JsonProperty("projetoId")
    private Long projectId;

    public static PPUEnhancedGridProjectionImpl fromBasicProjection(
            PPUProjection basic, 
            String branchName, 
            String clientName, 
            String contractName) {
        
        return new PPUEnhancedGridProjectionImpl(
            basic.getId(),
            basic.getCodeID(),
            basic.getNickname(),
            basic.getRegionalId(),
            branchName,
            basic.getClientId(),
            clientName,
            basic.getContract(),
            contractName,
            basic.getStatus(),
                null,
                basic.getProjectId()
        );
    }
}