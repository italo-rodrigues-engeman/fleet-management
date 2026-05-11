package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetEngenhariaRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetEngenhariaResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetEngenharia;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BudgetEngenhariaMapper {

    public BudgetEngenhariaResponseDTO toResponseDTO(BudgetEngenharia engenharia) {
        if (engenharia == null) {
            return null;
        }
        return BudgetEngenhariaResponseDTO.builder()
                .comentariosEngenharia(engenharia.getComentariosEngenharia())
                .anexoEngenharia(engenharia.getAnexoEngenharia())
                .responsavelEngenharia(engenharia.getResponsavelEngenharia())
                .createdAt(engenharia.getCreatedAt())
                .updatedAt(engenharia.getUpdatedAt())
                .createdBy(engenharia.getCreatedBy())
                .updatedBy(engenharia.getUpdatedBy())
                .build();
    }

    public List<BudgetEngenhariaResponseDTO> toResponseDTOList(List<BudgetEngenharia> engenhariaList) {
        if (engenhariaList == null) {
            return new ArrayList<>();
        }
        return engenhariaList.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BudgetEngenharia toEntity(BudgetEngenhariaRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BudgetEngenharia.builder()
                .comentariosEngenharia(dto.getComentariosEngenharia())
                .responsavelEngenharia(dto.getResponsavelEngenharia())
                .build();
    }
}

