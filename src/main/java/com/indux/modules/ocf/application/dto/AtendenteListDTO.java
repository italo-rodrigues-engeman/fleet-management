package com.indux.modules.ocf.application.dto;

import java.util.List;

public record AtendenteListDTO(
        Long id,
        Integer accountId,
        String email,
        String nome,
        String matricula,
        Long setor,
        List<String> regionais
) {}



