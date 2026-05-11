package com.indux.modules.organization_chart.application.dtos;

import java.util.List;

public record ContractWithProjectsDTO(
        Long id,
        String nome,
        String os,
        List<SimpleProjectDTO> projetos
) { }

