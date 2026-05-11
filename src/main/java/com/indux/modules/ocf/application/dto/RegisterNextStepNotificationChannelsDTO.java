package com.indux.modules.ocf.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * DTO para registrar canais de notificação para ocorrências que saem da primeira etapa
 */
public record RegisterNextStepNotificationChannelsDTO(
        @NotNull(message = "Canais são obrigatórios")
        List<String> canais,
        
        @NotNull(message = "User IDs são obrigatórios")
        List<String> userIds,
        
        @NotNull(message = "Texto da notificação é obrigatório")
        @NotBlank(message = "Texto da notificação não pode estar vazio")
        String textoNotificacao,
        
        @NotNull(message = "Tempo limite é obrigatório")
        @Positive(message = "Tempo limite deve ser um número positivo")
        Double tempoLimiteHoras,
        
        @NotNull(message = "Etapa de destino é obrigatória")
        @Positive(message = "Etapa de destino deve ser um número positivo")
        Integer etapaDestino
) {
    
    /**
     * Converte para CreateNotificationScheduleDTO para a etapa especificada
     */
    public CreateNotificationScheduleDTO toCreateNotificationScheduleDTO() {
        return new CreateNotificationScheduleDTO(
                this.canais,
                this.tempoLimiteHoras,
                this.etapaDestino,
                this.userIds,
                this.textoNotificacao
        );
    }
}
