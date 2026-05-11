package com.indux.modules.ocf.application.dto;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.modules.ocf.domain.model.NotificationSchedule;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationScheduleWithUsersDTO(
        String id,
        List<String> canais,
        Double tempoLimiteHoras,
        Integer etapa,
        List<SimpleUser> usuarios, // Informações completas dos usuários
        String textoNotificacao,
        Boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String criadoPor,
        String atualizadoPor
) {
    
    public static NotificationScheduleWithUsersDTO fromEntity(NotificationSchedule entity, List<SimpleUser> usuarios) {
        return new NotificationScheduleWithUsersDTO(
                entity.getId(),
                entity.getCanais(),
                entity.getTempoLimiteHoras(),
                entity.getEtapa(),
                usuarios,
                entity.getTextoNotificacao(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm(),
                entity.getCriadoPor(),
                entity.getAtualizadoPor()
        );
    }
}
