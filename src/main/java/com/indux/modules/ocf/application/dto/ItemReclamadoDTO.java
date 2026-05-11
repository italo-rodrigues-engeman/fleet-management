package com.indux.modules.ocf.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

public record ItemReclamadoDTO(
        String descricao,
        Integer quantidadeReclamada,
        BigDecimal valor,
        Optional<String> justificativa,
        Optional<String> observacao,
        Optional<String> respostaColab,
        Optional<MultipartFile> anexoInicial,      // Anexo que vem no FormData inicialmente
        Optional<MultipartFile> anexoAdicional,    // Anexo que será inserido depois
        Optional<MultipartFile> anexo              // Anexo genérico (para compatibilidade com frontend)
) {
} 