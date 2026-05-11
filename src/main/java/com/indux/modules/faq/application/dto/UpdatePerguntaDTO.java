package com.indux.modules.faq.application.dto;

import com.indux.core.domain.model.modules.AttachmentRecord;

import java.time.LocalDateTime;
import java.util.List;

public record UpdatePerguntaDTO(
        Long setorId,
        Long temaId,
        String categoria,
        String tipo,
        LocalDateTime dataCriacao,
        LocalDateTime dataFim,
        String titulo,
        Long regionalId,
        Long contratoId,
        java.util.List<Long> contratos,
        String publico,
        String aprovador,
        String observacoes,
        List<AttachmentRecord> anexos,
        List<CreatePerguntaDTO.RespostaItem> respostas,
        Boolean situacao
) {}


