package com.indux.core.application.dto.cbo;

import java.util.List;

public record FilterFuncao(
        List<Long> filialHCM,
        Long idAuto,
        List<String> codCBO,
        List<String> status,
        List<String> solicitante,
        String nome
) {
}
