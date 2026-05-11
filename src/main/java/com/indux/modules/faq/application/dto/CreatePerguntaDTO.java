package com.indux.modules.faq.application.dto;

import com.indux.core.domain.model.modules.AttachmentRecord;

import java.time.LocalDateTime;
import java.util.List;

public record CreatePerguntaDTO(
        Long setorId,
        Long temaId,
        String categoria,
        String tipo,
        LocalDateTime dataCriacao,
        LocalDateTime dataFim,
        String titulo,
        Long regionalId,
        Long contratoId,
        List<Long> contratos,
        String publico,
        String aprovador,
        String observacoes,
        List<AttachmentRecord> anexos,
        List<RespostaItem> respostas,
        Boolean situacao,
        Long diretoriaId,
        Long superintendenciaId,
        Long projetoId,
        List<Long> filialHcmId,
        Long setorOrganizationId,
        // listas (suporte a múltiplos)
        List<Long> diretoriaIds,
        List<Long> superintendenciaIds,
        List<Long> regionalIds,
        List<Long> setorOrganizationIds,
        List<Long> projetoIds
) {
    public record RespostaItem(
            String conteudo,
            Long regional,
            Long contrato,
            List<Long> contratos,
            String publico,
            List<AttachmentRecord> anexo,
            Long diretoriaId,
            Long superintendenciaId,
            Long projetoId,
            List<Long> filialHcmId,
            Long setorOrganizationId,
            // listas (suporte a múltiplos)
            List<Long> diretoriaIds,
            List<Long> superintendenciaIds,
            List<Long> regionalIds,
            List<Long> setorOrganizationIds,
            List<Long> projetoIds
    ) {}
}