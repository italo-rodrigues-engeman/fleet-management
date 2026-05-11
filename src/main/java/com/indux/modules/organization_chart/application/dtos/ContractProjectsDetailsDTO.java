package com.indux.modules.organization_chart.application.dtos;

import java.util.List;

public record ContractProjectsDetailsDTO(
        Long id,
        String nome,
        String os,
        List<ProjectWithDetailsDTO> projetos
) { }


