package com.indux.modules.organization_chart.application.dtos;

import java.util.List;

public record SimpleContractDTO(
        Long id,
        String nome,
        String os,
        List<SimpleProjectDTO> projetos
) { }
