package com.indux.modules.faq.application.dto;

public record RespostaDTO(
        Long id,
        String nome,
        Long perguntaId,
        String perguntaNome,
        Integer contrato,
        Boolean status
) {
    public static RespostaDTO fromEntity(com.indux.modules.faq.domain.entities.Resposta resposta) {
        return new RespostaDTO(
                resposta.getId(),
                resposta.getNome(),
                resposta.getPergunta().getId(),
                resposta.getPergunta().getNome(),
                resposta.getContrato(),
                resposta.getStatus()
        );
    }
} 