package com.indux.modules.ocf.application.mapper;

import com.indux.modules.ocf.application.dto.CreateNotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.dto.NotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.dto.UpdateNotificationScheduleConfigDTO;
import com.indux.modules.ocf.domain.model.NotificationScheduleConfig;
import org.springframework.stereotype.Component;

@Component("ocfNotificationScheduleConfigMapper")
public class OcfNotificationScheduleConfigMapper {
    
    public NotificationScheduleConfigDTO toDTO(NotificationScheduleConfig config) {
        return NotificationScheduleConfigDTO.builder()
                .id(config.getId())
                .diaDaSemana(config.getDiaDaSemana())
                .horaInicio(config.getHoraInicio())
                .horaFim(config.getHoraFim())
                .ativo(config.getAtivo())
                .build();
    }
    
    public NotificationScheduleConfig toEntity(CreateNotificationScheduleConfigDTO dto, String criadoPor) {
        return NotificationScheduleConfig.builder()
                .diaDaSemana(dto.getDiaDaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .ativo(dto.getAtivo())
                .criadoPor(criadoPor)
                .build();
    }
    
    public NotificationScheduleConfig toEntity(UpdateNotificationScheduleConfigDTO dto, String atualizadoPor) {
        return NotificationScheduleConfig.builder()
                .diaDaSemana(dto.getDiaDaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .ativo(dto.getAtivo())
                .atualizadoPor(atualizadoPor)
                .build();
    }
}
