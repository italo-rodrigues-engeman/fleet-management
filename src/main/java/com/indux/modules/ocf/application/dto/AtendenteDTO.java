package com.indux.modules.ocf.application.dto;

import java.util.List;

public record AtendenteDTO(
        String email,
        String matriculaRHlocal,
        String matriculaRHmatriz,
        Long setor,
        String atendenteRHmatriz,
        String atendenteRhlocal,
        String senha,
        List<String> regional,  // Mudando para lista de regionais
        List<Integer> contrato
) {
} 