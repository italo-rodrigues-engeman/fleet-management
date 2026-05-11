package com.indux.modules.ocf.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public record Analyst1ApprovalDTO(
        Optional<String> observacaoAnalista1,
        Optional<String> motivoAnalista,
        Optional<Boolean> aprovacaoAnalista1,
        Optional<MultipartFile> evidenciaAnalista,
        List<ItemReclamadoAnalyst1DTO> itemsreclamados,
        Optional<String> respostaEmpregado,
        Optional<String> email,
        Optional<String> telefone,
        Optional<List<String>> canais
) {
} 