package com.indux.modules.ocf.application.dto;

import com.indux.modules.ocf.domain.model.NotificationSchedule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

public record UpdateNotificationScheduleDTO(
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
        String textoNotificacao,
        Optional<Boolean> ativo
) {
    
    public void updateEntity(NotificationSchedule entity, String atualizadoPor) {
        entity.setCanais(this.canais);
        entity.setTempoLimiteHoras(this.tempoLimiteHoras);
        entity.setEtapa(this.etapa);
        entity.setUserIds(this.userIds);
        entity.setTextoNotificacao(this.textoNotificacao);
        this.ativo.ifPresent(entity::setAtivo);
        entity.setAtualizadoEm(java.time.LocalDateTime.now());
        entity.setAtualizadoPor(atualizadoPor);
    }
}
