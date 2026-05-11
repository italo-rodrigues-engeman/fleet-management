package com.indux.core.application.dto.module;

import java.util.Set;
import java.util.UUID;

public record ModuleResponseDTO(
        UUID id,
        String nome,
        String descricao,
        Integer quantidadeEtapas,
        Set<EtapaDTO> etapas,
        PermissoesUsuarioDTO permissoesUsuario,
        boolean administrador,
        boolean gerente,
        boolean favorito) {
    public record EtapaDTO(
            int numero,
            String nome,
            int tempoAceitavel) {
    }

    public record PermissoesUsuarioDTO(
            Set<Integer> etapasPermitidas,
            Set<Integer> acoesPermitidas,
            Set<Integer> regionaisPermitidas,
            Set<Integer> projetosPermitidos
            ) {
    }
}