package com.indux.core.domain.model.modules;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record AttachmentRecord(
        String nome, @NotNull(message = "O anexo é obrigatório.") MultipartFile file
) {
}
