package com.indux.modules.faq.application.dto;

public record CreateTemaDTO(
        String nome,
        Long setorId,
        String descricao,
        Boolean status,
        Integer ordenacao
) {}