package com.indux.modules.ocf.application.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public record ManagerApprovalDTO(
        Optional<String> observacaoGestor,
        Optional<String> causa,
        Optional<String> observacao,
        Optional<String> grupoResponsavel,
        Optional<MultipartFile> comprovanteDePagamento,
        Optional<String> respostaEmpregado,
        Optional<String> email,
        Optional<String> telefone,
        Optional<List<String>> canais,
        Optional<String> valorPagar,
        Optional<String> competenciaPagar,
        Optional<String> dataPagamento
) {
} 