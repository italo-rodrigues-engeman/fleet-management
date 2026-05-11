package com.indux.modules.ocf.application.dto;

import java.util.List;

public record AtendenteWithRegionalDTO(
        Long id,
        String email,
        String nome,
        String matricula,
        Integer accountId,
        String filial,
        String regional,
        List<String> regionais
) {}
