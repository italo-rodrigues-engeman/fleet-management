package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetOSResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetOS;
import org.springframework.stereotype.Component;

@Component
public class BudgetOSMapper {

    public BudgetOSResponseDTO toResponseDTO(BudgetOS os) {
        return BudgetOSResponseDTO.builder()
                .id(os.getId())
                .budgetId(os.getBudgetId())
                .numeroOs(os.getNumeroOs())
                .status(os.getStatus())
                .anexoOs(os.getAnexoOs())
                .cnpjDueDiligence(os.getCnpjDueDiligence())
                .inscEstadual(os.getInscEstadual())
                .cpfduediligence(os.getCpfduediligence())
                .createdAt(os.getCreatedAt())
                .updatedAt(os.getUpdatedAt())
                .createdBy(os.getCreatedBy())
                .updatedBy(os.getUpdatedBy())
                .build();
    }
}

