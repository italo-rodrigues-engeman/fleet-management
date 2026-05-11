package com.indux.modules.organization_chart.application.dtos;

import java.util.UUID;

public record SimpleEmployeeDTO(
        UUID id,
        String nome,
        String matricula,
        Integer filial_id_hcm
) { }
