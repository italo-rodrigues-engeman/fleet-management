package com.indux.modules.ocf.application.dto;

import java.util.List;

public record TimeWithMembersDTO(
        Long id,
        String nome,
        List<ContractSummaryDTO> contratos,
        List<AtendenteResumoDTO> atendentes
) {}



