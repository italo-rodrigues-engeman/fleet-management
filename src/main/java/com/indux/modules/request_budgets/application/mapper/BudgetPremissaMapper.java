package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetPremissaResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetPremissa;
import org.springframework.stereotype.Component;

@Component
public class BudgetPremissaMapper {

    public BudgetPremissaResponseDTO toResponseDTO(BudgetPremissa premissa) {
        return BudgetPremissaResponseDTO.builder()
                .id(premissa.getId())
                .versionId(premissa.getVersionId())
                .anexoTipo(premissa.getAnexoTipo())
                .anexoPremissa(premissa.getAnexoPremissa())
                .createdAt(premissa.getCreatedAt())
                .updatedAt(premissa.getUpdatedAt())
                .createdBy(premissa.getCreatedBy())
                .updatedBy(premissa.getUpdatedBy())
                .build();
    }
}

