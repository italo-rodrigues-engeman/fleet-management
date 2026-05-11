package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemSolicitationListDTO {
    
    @JsonProperty("id")
    private String id;

    @JsonProperty("sequentialId")
    private Long sequentialId;
    
    @JsonProperty("nomePrincipal")
    private String mainName;
    
    @JsonProperty("descricao")
    private String description;
    
    @JsonProperty("regional")
    private String regional;
    
    @JsonProperty("status")
    private ItemSolicitationStatus status;
    
    @JsonProperty("solicitante")
    private String applicant;
    
    @JsonProperty("dataCriacao")
    private LocalDateTime creationDate;
    
    @JsonProperty("etapa")
    private Integer etapa;
}

