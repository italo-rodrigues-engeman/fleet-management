package com.indux.modules.budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSimpleBudgetRequest {
    @NotNull(message = "Cliente ID é obrigatório")
    private Long clienteId;
    private String clienteNome;
    private Long setorId;
    private String acOs;
    private String setor;
    private String orcamentista;
    private Integer oportunidade;
    @NotNull(message = "Nome da oportunidade é obrigatório")
    private String nomeOportunidade;
}
