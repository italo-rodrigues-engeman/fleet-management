package com.indux.modules.organization_chart.application.dtos;

import java.util.List;

public record SimpleProjectDTO(
        Long id,
        Integer hcm,
        boolean ativo,
        List<SimpleEmployeeDTO> funcionarios
) { }
