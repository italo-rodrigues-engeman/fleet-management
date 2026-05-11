package com.indux.modules.faq.application.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FAQReviewRequest(
        // Campos básicos da pergunta
        Long setorId,
        Long temaId,
        String categoria,
        String tipo,
        LocalDateTime dataCriacao,
        LocalDateTime dataFim,
        String titulo,
        Long regionalId,
        List<Long> contratos,
        String publico,
        String aprovador,
        String observacoes,
        
        // Anexos como MultipartFile (serão convertidos no service)
        List<MultipartFile> anexos,
        List<String> anexosRemover,
        
        // Respostas
        List<RespostaItem> respostas,
        
        // Campo situação
        Boolean situacao,
        
        // Campo específico para revisão
        String motivoRevisao,
        
        // Novos campos solicitados
        Long diretoriaId,
        Long superintendenciaId,
        Long projetoId,
        List<Long> filialHcmId,  // Changed to List<Long>
        Long setorOrganizationId
) {
    public record RespostaItem(
            String conteudo,
            Long regional,
            List<Long> contratos,
            String publico,
            List<MultipartFile> anexo,  // MultipartFile em vez de AttachmentRecord
            List<String> anexosRemover,  // IDs dos anexos a serem removidos
            
            // Novos campos para resposta
            Long diretoriaId,
            Long superintendenciaId,
            Long projetoId,
            List<Long> filialHcmId,  // Changed to List<Long>
            Long setorOrganizationId
    ) {}
}