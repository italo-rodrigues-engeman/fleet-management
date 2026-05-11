package com.indux.modules.budgets.application.mapper;

import com.indux.modules.budgets.application.dto.SimpleBudgetResponseDTO;
import com.indux.modules.budgets.application.dto.SimpleBudgetSummaryDTO;
import com.indux.modules.budgets.domain.model.SimpleBudget;
import org.springframework.stereotype.Component;

@Component
public class SimpleBudgetMapper {

    public SimpleBudgetResponseDTO toResponseDTO(SimpleBudget budget) {
        return SimpleBudgetResponseDTO.builder()
                .id(budget.getId())
                .clienteId(budget.getClienteId())
                .clienteNome(budget.getClienteNome())
                .setorId(budget.getSetorId())
                .acOs(budget.getAcOs())
                .setor(budget.getSetor())
                .orcamentista(budget.getOrcamentista())
                .oportunidade(budget.getOportunidade())
                .nomeOportunidade(budget.getNomeOportunidade())
                .status(budget.getStatus())
                .observacao(budget.getObservacao())
                .step(budget.getStep())
                .stepLog(budget.getStepLog())
                .itens(budget.getItens())
                .servicos(budget.getServicos())
                .build();
    }

    public SimpleBudgetSummaryDTO toSummaryDTO(SimpleBudget budget) {
        return SimpleBudgetSummaryDTO.builder()
                .id(budget.getId())
                .nomeOportunidade(budget.getNomeOportunidade())
                .clienteId(budget.getClienteId())
                .clienteNome(budget.getClienteNome())
                .oportunidade(budget.getOportunidade())
                .status(budget.getStatus())
                .step(budget.getStep())
                .orcamentista(budget.getOrcamentista())
                .dataCriacao(budget.getStepLog() != null ? budget.getStepLog().stream()
                        .filter(log -> "CRIACAO_ORCAMENTO".equals(log.getName()))
                        .map(com.indux.core.domain.model.modules.form.StepLog::getCreated_at)
                        .findFirst()
                        .orElse(null) : null)
                .build();
    }
}
