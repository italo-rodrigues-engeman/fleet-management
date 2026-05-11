package com.indux.core.application.dto;

import com.indux.core.domain.model.NotificationScheduleConfig;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationScheduleConfigRequestDTO {
    
    @NotEmpty(message = "Dias da semana são obrigatórios")
    private List<NotificationScheduleConfig.DayOfWeek> diasDaSemana;
    
    @NotNull(message = "Hora de início é obrigatória")
    private LocalTime horaInicio;
    
    @NotNull(message = "Hora de fim é obrigatória")
    private LocalTime horaFim;
    
    @Builder.Default
    private boolean ativo = true;
}
