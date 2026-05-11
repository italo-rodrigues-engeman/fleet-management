package com.indux.modules.faq.application.dto;

public record SetorDTO(
        Long id,
        String nome,
        Boolean status,
        String descricao
) {
    public static SetorDTO fromEntity(com.indux.modules.faq.domain.entities.Setor setor) {
        return new SetorDTO(
                setor.getId(),
                setor.getNome(),
                setor.getStatus(),
                setor.getDescricao()
        );
    }
} 