package com.indux.modules.faq.application.dto;

public record PerguntaDTO(
        Long id,
        String nome,
        Long temaId,
        String temaNome,
        Long setorId,
        String setorNome
) {
    public static PerguntaDTO fromEntity(com.indux.modules.faq.domain.entities.Pergunta pergunta) {
        return new PerguntaDTO(
                pergunta.getId(),
                pergunta.getNome(),
                pergunta.getTema().getId(),
                pergunta.getTema().getNome(),
                pergunta.getTema().getSetor().getId(),
                pergunta.getTema().getSetor().getNome()
        );
    }
} 