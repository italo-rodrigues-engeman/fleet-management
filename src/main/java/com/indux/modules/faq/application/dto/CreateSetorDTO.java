package com.indux.modules.faq.application.dto;

public record CreateSetorDTO(
        String nome,
        Boolean status,
        String descricao
) {}