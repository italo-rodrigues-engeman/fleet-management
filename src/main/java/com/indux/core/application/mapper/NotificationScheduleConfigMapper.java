package com.indux.core.application.mapper;

import com.indux.core.application.dto.NotificationScheduleConfigRequestDTO;
import com.indux.core.application.dto.NotificationScheduleConfigResponseDTO;
import com.indux.core.domain.model.NotificationScheduleConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationScheduleConfigMapper {
    
    public NotificationScheduleConfig toEntity(NotificationScheduleConfigRequestDTO dto, String usuarioCriacao) {
        return NotificationScheduleConfig.builder()
                .diasDaSemana(dto.getDiasDaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .ativo(dto.isAtivo())
                .criadoPor(usuarioCriacao)
                .criadoEm(LocalDateTime.now())
                .build();
    }
    
    public NotificationScheduleConfigResponseDTO toResponseDTO(NotificationScheduleConfig entity) {
        return NotificationScheduleConfigResponseDTO.builder()
                .id(entity.getId())
                .diasDaSemana(entity.getDiasDaSemana())
                .horaInicio(entity.getHoraInicio())
                .horaFim(entity.getHoraFim())
                .ativo(entity.isAtivo())
                .criadoPor(entity.getCriadoPor())
                .atualizadoPor(entity.getAtualizadoPor())
                .criadoEm(entity.getCriadoEm())
                .atualizadoEm(entity.getAtualizadoEm())
                .build();
    }
    
    public void updateEntity(NotificationScheduleConfig entity, NotificationScheduleConfigRequestDTO dto, String usuarioAtualizacao) {
        entity.setDiasDaSemana(dto.getDiasDaSemana());
        entity.setHoraInicio(dto.getHoraInicio());
        entity.setHoraFim(dto.getHoraFim());
        entity.setAtivo(dto.isAtivo());
        entity.setAtualizadoPor(usuarioAtualizacao);
        entity.setAtualizadoEm(LocalDateTime.now());
    }
}
