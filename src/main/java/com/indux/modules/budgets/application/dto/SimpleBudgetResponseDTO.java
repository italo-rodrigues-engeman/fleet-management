package com.indux.modules.budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.budgets.domain.model.SimpleBudgetItem;
import com.indux.modules.budgets.domain.model.SimpleBudgetServiceItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleBudgetResponseDTO {
    private String id;
    private Long clienteId;
    private String clienteNome;
    private Long mercadoId;
    private Long setorId;

    private String acOs;
    private String setor;
    private String orcamentista;
    private Integer oportunidade;
    private String nomeOportunidade;
    private String status;
    @com.fasterxml.jackson.annotation.JsonProperty("observation")
    private String observacao;
    private Integer step;
    private List<SimpleBudgetItem> itens;
    private List<SimpleBudgetServiceItem> servicos;
    private List<StepLog> stepLog;
}
