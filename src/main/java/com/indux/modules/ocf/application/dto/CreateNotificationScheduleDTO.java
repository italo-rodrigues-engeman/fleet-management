package com.indux.modules.ocf.application.dto;

import com.indux.modules.ocf.domain.model.NotificationSchedule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateNotificationScheduleDTO(
        @NotNull(message = "Canais são obrigatórios")
        List<String> canais,
        @NotNull(message = "Tempo limite é obrigatório")
        @Positive(message = "Tempo limite deve ser um número positivo")
        Double tempoLimiteHoras,
        @NotNull(message = "Etapa é obrigatória")
        @Positive(message = "Etapa deve ser um número positivo")
        Integer etapa,
        @NotNull(message = "User IDs são obrigatórios")
        List<String> userIds,
        @NotNull(message = "Texto da notificação é obrigatório")
        String textoNotificacao
) {
    
    public NotificationSchedule toEntity(String criadoPor) {
        return new NotificationSchedule(this.canais, this.tempoLimiteHoras, this.etapa, this.userIds, this.textoNotificacao, criadoPor);
    }
}
