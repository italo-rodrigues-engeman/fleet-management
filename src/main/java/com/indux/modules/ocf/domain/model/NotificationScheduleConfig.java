package com.indux.modules.ocf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_schedule_configs")
public class NotificationScheduleConfig {
    
    @Id
    private String id;
    
    @Field(name = "nome")
    private String nome;
    
    @Field(name = "descricao")
    private String descricao;
    
    @Field(name = "dia_da_semana")
    private DayOfWeek diaDaSemana;
    
    @Field(name = "hora_inicio")
    private LocalTime horaInicio;
    
    @Field(name = "hora_fim")
    private LocalTime horaFim;
    
    @Field(name = "ativo")
    private Boolean ativo;
    
    @Field(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Field(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
    
    @Field(name = "criado_por")
    private String criadoPor;
    
    @Field(name = "atualizado_por")
    private String atualizadoPor;
    
    public boolean isNotificationEnabled() {
        if (!ativo || diaDaSemana == null) {
            return false;
        }
        
        LocalDateTime agora = LocalDateTime.now();
        DayOfWeek diaAtual = agora.toLocalDate().getDayOfWeek();
        LocalTime horaAtual = agora.toLocalTime();
        
        return diaDaSemana.equals(diaAtual) && 
               horaAtual.isAfter(horaInicio) && 
               horaAtual.isBefore(horaFim);
    }
}
