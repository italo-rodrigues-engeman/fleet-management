package com.indux.modules.ocf.application.dto;

import java.util.List;
import java.util.Optional;

public record AtendentePatchDTO(
        Optional<String> email,
        Optional<String> matriculaRHlocal,
        Optional<String> matriculaRHmatriz,
        Optional<Long> setor,
        Optional<String> atendenteRHmatriz,
        Optional<String> atendenteRhlocal,
        Optional<String> senha,
        Optional<List<String>> regional,
        Optional<List<Integer>> contrato
) {
    public static AtendentePatchDTO empty() {
        return new AtendentePatchDTO(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }
} 