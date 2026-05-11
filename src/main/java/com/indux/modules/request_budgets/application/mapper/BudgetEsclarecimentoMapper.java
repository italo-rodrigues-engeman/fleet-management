package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetEsclarecimentoResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetEsclarecimento;
import org.springframework.stereotype.Component;

@Component
public class BudgetEsclarecimentoMapper {

    public BudgetEsclarecimentoResponseDTO toResponseDTO(BudgetEsclarecimento esclarecimento) {
        return BudgetEsclarecimentoResponseDTO.builder()
                .id(esclarecimento.getId())
                .versionId(esclarecimento.getVersionId())
                .perguntas(esclarecimento.getPerguntas())
                .anexoEsclarecimento(esclarecimento.getAnexoEsclarecimento())
                .descricaoEsclarecimento(esclarecimento.getDescricaoEsclarecimento())
                .dataHoraEsclarecimento(esclarecimento.getDataHoraEsclarecimento())
                .responsavelEsclarecimento(esclarecimento.getResponsavelEsclarecimento())
                .createdAt(esclarecimento.getCreatedAt())
                .updatedAt(esclarecimento.getUpdatedAt())
                .createdBy(esclarecimento.getCreatedBy())
                .updatedBy(esclarecimento.getUpdatedBy())
                .build();
    }
}

