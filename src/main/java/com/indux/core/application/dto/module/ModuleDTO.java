package com.indux.core.application.dto.module;

import java.util.List;

public record ModuleDTO(
        String id,
        String nome,
        String descricao,
        Integer quantidadeEtapas,
        List<Integer> etapasPermitidas,
        List<Integer> filiaisPermitidas,
        List<Integer> contratosPermitidos,
        List<Integer> acoesPermitidas,
        boolean status
) {
}
