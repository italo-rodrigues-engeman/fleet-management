package com.indux.modules.ocf.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public record AnalystApprovalDTO(
        Optional<String> observacaoAnalista,
        Optional<MultipartFile> evidenciaAnalista,
        Optional<String> respostaEmpregado,
        Optional<String> email,
        Optional<String> telefone,
        Optional<List<String>> canais
) {
} 