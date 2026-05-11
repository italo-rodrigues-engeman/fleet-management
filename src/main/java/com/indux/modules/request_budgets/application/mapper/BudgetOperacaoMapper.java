package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetOperacaoRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetOperacaoResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetOperacao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BudgetOperacaoMapper {

    public BudgetOperacaoResponseDTO toResponseDTO(BudgetOperacao operacao) {
        if (operacao == null) {
            return null;
        }
        return BudgetOperacaoResponseDTO.builder()
                .comentariosOperacao(operacao.getComentariosOperacao())
                .anexoOperacao(operacao.getAnexoOperacao())
                .datahoraOperacao(operacao.getDatahoraOperacao())
                .responsavelOperacao(operacao.getResponsavelOperacao())
                .createdAt(operacao.getCreatedAt())
                .updatedAt(operacao.getUpdatedAt())
                .createdBy(operacao.getCreatedBy())
                .updatedBy(operacao.getUpdatedBy())
                .build();
    }

    public List<BudgetOperacaoResponseDTO> toResponseDTOList(List<BudgetOperacao> operacaoList) {
        if (operacaoList == null) {
            return new ArrayList<>();
        }
        return operacaoList.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BudgetOperacao toEntity(BudgetOperacaoRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BudgetOperacao.builder()
                .comentariosOperacao(dto.getComentariosOperacao())
                .datahoraOperacao(dto.getDatahoraOperacao())
                .responsavelOperacao(dto.getResponsavelOperacao())
                .build();
    }
}

