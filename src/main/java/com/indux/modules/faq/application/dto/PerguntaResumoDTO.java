package com.indux.modules.faq.application.dto;

import java.time.LocalDateTime;

public record PerguntaResumoDTO(
        Long id,
        String nome_setor,
        String nome_tema,
        String titulo,
        Integer total_respostas,
        String status,
        String categoria,
        String regionalName,
        Integer etapa_atual,
        LocalDateTime dataCriacao,
        LocalDateTime dataFim,
        Boolean situacao,
        String idMongo
) {}


