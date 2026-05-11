package com.indux.modules.faq.application.dto;

public record CreateRespostaDTO(
        String nome,
        Long perguntaId,
        Integer contrato,
        Boolean status
) {} 