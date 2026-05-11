package com.indux.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_schedule_config")
public class NotificationScheduleConfig {
    
    @Id
    private String id;
    
    private List<DayOfWeek> diasDaSemana;
    
    private LocalTime horaInicio;
    
    private LocalTime horaFim;
    
    private boolean ativo;
    
    private String criadoPor;
    
    private String atualizadoPor;
    
    private java.time.LocalDateTime criadoEm;
    
    private java.time.LocalDateTime atualizadoEm;
    
    public enum DayOfWeek {
        SEGUNDA_FEIRA,
        TERCA_FEIRA,
        QUARTA_FEIRA,
        QUINTA_FEIRA,
        SEXTA_FEIRA,
        SABADO,
        DOMINGO
    }
}
