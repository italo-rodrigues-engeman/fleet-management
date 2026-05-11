package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetSmsRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetSmsResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetSms;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BudgetSmsMapper {

    public BudgetSmsResponseDTO toResponseDTO(BudgetSms sms) {
        if (sms == null) {
            return null;
        }
        return BudgetSmsResponseDTO.builder()
                .comentariosSms(sms.getComentariosSms())
                .anexoSms(sms.getAnexoSms())
                .datahoraSms(sms.getDatahoraSms())
                .responsavelSms(sms.getResponsavelSms())
                .createdAt(sms.getCreatedAt())
                .updatedAt(sms.getUpdatedAt())
                .createdBy(sms.getCreatedBy())
                .updatedBy(sms.getUpdatedBy())
                .build();
    }

    public List<BudgetSmsResponseDTO> toResponseDTOList(List<BudgetSms> smsList) {
        if (smsList == null) {
            return new ArrayList<>();
        }
        return smsList.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BudgetSms toEntity(BudgetSmsRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BudgetSms.builder()
                .comentariosSms(dto.getComentariosSms())
                .datahoraSms(dto.getDatahoraSms())
                .responsavelSms(dto.getResponsavelSms())
                .build();
    }
}

