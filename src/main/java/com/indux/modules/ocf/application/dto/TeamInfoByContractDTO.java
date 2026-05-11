package com.indux.modules.ocf.application.dto;

import java.util.List;

public record TeamInfoByContractDTO(
        Long teamId,
        String teamName,
        List<AtendenteWithRegionalDTO> atendentes
) {}
