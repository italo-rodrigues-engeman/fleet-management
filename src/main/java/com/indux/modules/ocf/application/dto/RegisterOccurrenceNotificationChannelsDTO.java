package com.indux.modules.ocf.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * DTO para registrar canais de notificação para ocorrências criadas
 */
public record RegisterOccurrenceNotificationChannelsDTO(
        @NotNull(message = "Canais são obrigatórios")
        List<String> canais,
        
        @NotNull(message = "User IDs são obrigatórios")
        List<String> userIds,
        
        @NotNull(message = "Texto da notificação é obrigatório")
        @NotBlank(message = "Texto da notificação não pode estar vazio")
        String textoNotificacao,
        
        @NotNull(message = "Tempo limite é obrigatório")
        @Positive(message = "Tempo limite deve ser um número positivo")
        Double tempoLimiteHoras
) {
    
    /**
     * Converte para CreateNotificationScheduleDTO para etapa 1 (criação)
     */
    public CreateNotificationScheduleDTO toCreateNotificationScheduleDTO() {
        return new CreateNotificationScheduleDTO(
                this.canais,
                this.tempoLimiteHoras,
                1, // Etapa 1 = criação da ocorrência
                this.userIds,
                this.textoNotificacao
        );
    }
}
