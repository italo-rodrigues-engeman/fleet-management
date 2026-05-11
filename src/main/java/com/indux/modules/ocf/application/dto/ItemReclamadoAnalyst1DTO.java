package com.indux.modules.ocf.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public record ItemReclamadoAnalyst1DTO(
        String id,                                    // ID do item reclamado
        Optional<String> justificativa,               // Justificativa do analista
        Optional<String> observacao,                  // Observação do analista
        Optional<String> respostaColab,               // Resposta do colaborador
        Optional<MultipartFile> anexoAdicional       // Anexo adicional do analista
) {
} 