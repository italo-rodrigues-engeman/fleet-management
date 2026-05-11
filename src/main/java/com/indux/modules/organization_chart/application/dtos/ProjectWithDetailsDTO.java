package com.indux.modules.organization_chart.application.dtos;

import java.util.List;

public record ProjectWithDetailsDTO(
        Long id,
        Integer megaId,
        String megaNome,
        Integer hcmId,
        String nomeCentroCusto,
        List<FilialInfoDTO> filiais,
        boolean ativo
) { }


