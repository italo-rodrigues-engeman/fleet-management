package com.indux.modules.whatsapp_media.domain.entity;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record WhatsappMediaRecord(
        @NotNull(message = "Item obrigatório") MultipartFile file

        )
{
}
