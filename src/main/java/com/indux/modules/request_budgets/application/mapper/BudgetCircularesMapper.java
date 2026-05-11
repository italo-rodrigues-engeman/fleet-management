package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetCircularesResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetCirculares;
import org.springframework.stereotype.Component;

@Component
public class BudgetCircularesMapper {

    public BudgetCircularesResponseDTO toResponseDTO(BudgetCirculares circulares) {
        return BudgetCircularesResponseDTO.builder()
                .id(circulares.getId())
                .versionId(circulares.getVersionId())
                .identificacaoCirculares(circulares.getIdentificacaoCirculares())
                .anexoCirculares(circulares.getAnexoCirculares())
                .descricaoCirculares(circulares.getDescricaoCirculares())
                .dataHoraCirculares(circulares.getDataHoraCirculares())
                .responsavelCirculares(circulares.getResponsavelCirculares())
                .createdAt(circulares.getCreatedAt())
                .updatedAt(circulares.getUpdatedAt())
                .createdBy(circulares.getCreatedBy())
                .updatedBy(circulares.getUpdatedBy())
                .build();
    }
}

