package com.indux.modules.faq.application.dto;

public record TemaDTO(
        Long id,
        String nome,
        Long setorId,
        String setorNome,
        String descricao,
        Boolean status,
        Integer ordenacao
) {
    public static TemaDTO fromEntity(com.indux.modules.faq.domain.entities.Tema tema) {
        return new TemaDTO(
                tema.getId(),
                tema.getNome(),
                tema.getSetor().getId(),
                tema.getSetor().getNome(),
                tema.getDescricao(),
                tema.getStatus(),
                tema.getOrdenacao()
        );
    }
} 