package com.indux.modules.ocf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_inactivity_states")
public class NotificationInactivityState {
    
    @Id
    private String id;
    
    @Field(name = "config_id")
    private String configId;
    
    @Field(name = "inicio_inatividade")
    private LocalDateTime inicioInatividade;
    
    @Field(name = "fim_inatividade")
    private LocalDateTime fimInatividade;
    
    @Field(name = "ocorrencias_pendentes")
    private List<String> ocorrenciasPendentes;
    
    @Field(name = "agendamentos_pendentes")
    private List<String> agendamentosPendentes;
    
    @Field(name = "processado")
    private Boolean processado;
    
    @Field(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Field(name = "processado_em")
    private LocalDateTime processadoEm;
    
    public static NotificationInactivityState createInactivityStart(String configId, LocalDateTime inicio) {
        return NotificationInactivityState.builder()
                .configId(configId)
                .inicioInatividade(inicio)
                .ocorrenciasPendentes(List.of())
                .agendamentosPendentes(List.of())
                .processado(false)
                .criadoEm(LocalDateTime.now())
                .build();
    }
}
