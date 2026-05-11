package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetComercialRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetComercialResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetComercial;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BudgetComercialMapper {

    public BudgetComercialResponseDTO toResponseDTO(BudgetComercial comercial) {
        if (comercial == null) {
            return null;
        }
        return BudgetComercialResponseDTO.builder()
                .comentariosComercial(comercial.getComentariosComercial())
                .anexoComercial(comercial.getAnexoComercial())
                .dataHoraComercial(comercial.getDataHoraComercial())
                .responsavelComercial(comercial.getResponsavelComercial())
                .createdAt(comercial.getCreatedAt())
                .updatedAt(comercial.getUpdatedAt())
                .createdBy(comercial.getCreatedBy())
                .updatedBy(comercial.getUpdatedBy())
                .build();
    }

    public List<BudgetComercialResponseDTO> toResponseDTOList(List<BudgetComercial> comercialList) {
        if (comercialList == null) {
            return new ArrayList<>();
        }
        return comercialList.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BudgetComercial toEntity(BudgetComercialRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BudgetComercial.builder()
                .comentariosComercial(dto.getComentariosComercial())
                .dataHoraComercial(dto.getDataHoraComercial())
                .responsavelComercial(dto.getResponsavelComercial())
                .build();
    }
}

