package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Filtro1RequestDTO {
    private String filtro1;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datahoraFiltro1;
    
    private String responsavelFiltro1;
    private List<AttachmentEntity> anexoFiltro1;
    private String motivoFiltro1;
    private String responsavelFilrtro1;
    
    // Campos adicionais
    private String numeroAc;
    private String oracamentista;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataDesignacao;
    
    private String justificativaEngeman;
    private String justificativaSolicitante;
}

