package com.indux.modules.ocf.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationRequest(
        @NotNull(message = "Canais de notificação são obrigatórios")
        @NotEmpty(message = "Pelo menos um canal deve ser especificado")
        List<String> canais, // ["EMAIL", "WHATSAPP", "SMS"]
        
        @NotNull(message = "Texto da resposta é obrigatório")
        String respostaEmpregado
) {
}

