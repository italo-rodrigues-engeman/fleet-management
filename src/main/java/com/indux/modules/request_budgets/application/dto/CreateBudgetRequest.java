package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBudgetRequest {
    @NotNull(message = "Cliente ID é obrigatório")
    private Long clienteId;

    @NotNull(message = "Mercado ID é obrigatório")
    private Long mercadoId;

    @NotNull(message = "Setor ID é obrigatório")
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

    @NotNull(message = "Nome da oportunidade é obrigatório")
    private String nomeOportunidade;

    private String descricaoOportunidade;

    private String tempoContrato;

    private BigDecimal porteEstimado;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataAbertura;

    @NotNull(message = "Representante comercial ID é obrigatório")
    private UUID representanteComercialId;

    private String detalhesGerais;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAcompanhamento;

    private List<BudgetSolicitanteDTO> solicitantes;

    private List<BudgetDocumentoDTO> documentos;
}

