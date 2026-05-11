package com.indux.core.application.dto;

import com.indux.core.domain.model.NotificationScheduleConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationScheduleConfigResponseDTO {
    
    private String id;
    
    private List<NotificationScheduleConfig.DayOfWeek> diasDaSemana;
    
    private LocalTime horaInicio;
    
    private LocalTime horaFim;
    
    private boolean ativo;
    
    private String criadoPor;
    
    private String atualizadoPor;
    
    private LocalDateTime criadoEm;
    
    private LocalDateTime atualizadoEm;
}
