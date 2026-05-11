package com.indux.core.application.service;

import com.indux.core.application.dto.NotificationScheduleConfigRequestDTO;
import com.indux.core.application.dto.NotificationScheduleConfigResponseDTO;
import com.indux.core.application.mapper.NotificationScheduleConfigMapper;
import com.indux.core.domain.model.NotificationScheduleConfig;
import com.indux.core.domain.repository.NotificationScheduleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationScheduleConfigService {
    
    private final NotificationScheduleConfigRepository repository;
    private final NotificationScheduleConfigMapper mapper;
    
    public NotificationScheduleConfigResponseDTO createOrUpdateConfig(NotificationScheduleConfigRequestDTO request, String usuario) {
        validateTimeRange(request.getHoraInicio(), request.getHoraFim());
        
        Optional<NotificationScheduleConfig> existingConfig = repository.findByAtivoTrue();
        
        if (existingConfig.isPresent()) {
            NotificationScheduleConfig config = existingConfig.get();
            mapper.updateEntity(config, request, usuario);
            NotificationScheduleConfig savedConfig = repository.save(config);
            return mapper.toResponseDTO(savedConfig);
        } else {
            NotificationScheduleConfig newConfig = mapper.toEntity(request, usuario);
            NotificationScheduleConfig savedConfig = repository.save(newConfig);
            return mapper.toResponseDTO(savedConfig);
        }
    }
    
    public Optional<NotificationScheduleConfigResponseDTO> getActiveConfig() {
        return repository.findByAtivoTrue()
                .map(mapper::toResponseDTO);
    }
    
    public void deactivateConfig(String usuario) {
        Optional<NotificationScheduleConfig> existingConfig = repository.findByAtivoTrue();
        if (existingConfig.isPresent()) {
            NotificationScheduleConfig config = existingConfig.get();
            config.setAtivo(false);
            config.setAtualizadoPor(usuario);
            config.setAtualizadoEm(java.time.LocalDateTime.now());
            repository.save(config);
        }
    }
    
    public boolean isNotificationAllowed() {
        Optional<NotificationScheduleConfig> config = repository.findByAtivoTrue();
        if (config.isEmpty()) {
            return true;
        }
        
        NotificationScheduleConfig activeConfig = config.get();
        if (!activeConfig.isAtivo()) {
            return false;
        }
        
        java.time.DayOfWeek currentDay = java.time.LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = java.time.LocalTime.now();
        
        boolean isAllowedDay = activeConfig.getDiasDaSemana().stream()
                .anyMatch(day -> mapToJavaDayOfWeek(day) == currentDay);
        
        if (!isAllowedDay) {
            return false;
        }
        
        return !currentTime.isBefore(activeConfig.getHoraInicio()) && 
               !currentTime.isAfter(activeConfig.getHoraFim());
    }
    
    private void validateTimeRange(LocalTime horaInicio, LocalTime horaFim) {
        if (horaInicio.isAfter(horaFim)) {
            throw new IllegalArgumentException("Hora de início deve ser anterior à hora de fim");
        }
        
        if (horaInicio.equals(horaFim)) {
            throw new IllegalArgumentException("Hora de início e fim não podem ser iguais");
        }
    }
    
    private java.time.DayOfWeek mapToJavaDayOfWeek(NotificationScheduleConfig.DayOfWeek day) {
        return switch (day) {
            case SEGUNDA_FEIRA -> java.time.DayOfWeek.MONDAY;
            case TERCA_FEIRA -> java.time.DayOfWeek.TUESDAY;
            case QUARTA_FEIRA -> java.time.DayOfWeek.WEDNESDAY;
            case QUINTA_FEIRA -> java.time.DayOfWeek.THURSDAY;
            case SEXTA_FEIRA -> java.time.DayOfWeek.FRIDAY;
            case SABADO -> java.time.DayOfWeek.SATURDAY;
            case DOMINGO -> java.time.DayOfWeek.SUNDAY;
        };
    }
}
