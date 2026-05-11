package com.indux.modules.ocf.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationScheduleConfigDTO {
    
    
    @NotNull(message = "Dia da semana é obrigatório")
    private DayOfWeek diaDaSemana;
    
    @NotNull(message = "Hora de início é obrigatória")
    private LocalTime horaInicio;
    
    @NotNull(message = "Hora de fim é obrigatória")
    private LocalTime horaFim;
    
    @Builder.Default
    private Boolean ativo = true;
}
