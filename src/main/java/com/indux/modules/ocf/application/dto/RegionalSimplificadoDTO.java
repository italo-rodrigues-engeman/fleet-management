package com.indux.modules.ocf.application.dto;

import com.indux.core.domain.model.employee.Regional;

public record RegionalSimplificadoDTO(
        Long id,
        String regional,
        String matriculaResponsavel,
        java.util.List<FilialResumoDTO> filiais
) {
    
    public static RegionalSimplificadoDTO fromEntity(Regional regional) {
        if (regional == null) {
            return null;
        }
        java.util.List<FilialResumoDTO> filiais = null;
        if (regional.getFiliais() != null) {
            filiais = regional.getFiliais().stream()
                    .map(f -> new FilialResumoDTO(f.getBranchId(), f.getBranchName()))
                    .toList();
        }
        return new RegionalSimplificadoDTO(
                regional.getId(),
                regional.getRegional(),
                regional.getMatriculaResponsavel(),
                filiais
        );
    }
} 