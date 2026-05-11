package com.indux.modules.ocf.application.dto;

import com.indux.modules.ocf.domain.model.NotificationSchedule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationScheduleDTO(
        String id,
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
        Boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String criadoPor,
        String atualizadoPor
) {
    
    public static NotificationScheduleDTO fromEntity(NotificationSchedule entity) {
        return new NotificationScheduleDTO(
                entity.getId(),
                entity.getCanais(),
                entity.getTempoLimiteHoras(),
                entity.getEtapa(),
                entity.getUserIds(),
                entity.getTextoNotificacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm(),
                entity.getCriadoPor(),
                entity.getAtualizadoPor()
        );
    }
    
    public NotificationSchedule toEntity() {
        NotificationSchedule entity = new NotificationSchedule();
        entity.setId(this.id);
        entity.setCanais(this.canais);
        entity.setTempoLimiteHoras(this.tempoLimiteHoras);
        entity.setEtapa(this.etapa);
        entity.setUserIds(this.userIds);
        entity.setTextoNotificacao(this.textoNotificacao);
        entity.setAtivo(this.ativo != null ? this.ativo : true);
        entity.setCriadoEm(this.criadoEm);
        entity.setAtualizadoEm(this.atualizadoEm);
        entity.setCriadoPor(this.criadoPor);
        entity.setAtualizadoPor(this.atualizadoPor);
        return entity;
    }
}
