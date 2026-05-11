package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetResponseDTO {
    private String id;
    private Long clienteId;
    private Long mercadoId;
    private Long setorId;
    
    // Campos do solicitante
    private String solicitanteNome;
    private String solicitanteFuncaoCargo;
    private String solicitanteTelefone1;
    private String solicitanteTelefone2;
    private String solicitanteEmail1;
    private String solicitanteEmail2;
    private String solicitanteLocalizacao;
    private String solicitanteObservacao;
    
    private List<BudgetSolicitanteDTO> solicitantes;
    private String nomeOportunidade;
    private String descricaoOportunidade;
    private String tempoContrato;
    private BigDecimal porteEstimado;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataAbertura;
    
    private UUID representanteComercialId;
    private String detalhesGerais;
    
    private List<BudgetDocumentoDTO> documentos;
    
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAcompanhamento;
    
    private List<StepLogEmbeddedDTO> stepLog;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
    private UUID updatedBy;

    @Data
    @Builder
    public static class StepLogEmbeddedDTO {
        private String id;
        private String name;
        private Integer step;
        private Date created_at;
        private String user;
        private String observation;
        private Integer stepCounter;
    }
}

