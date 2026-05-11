package com.indux.core.application.service;

import com.indux.core.application.dto.NotificationScheduleConfigResponseDTO;
import com.indux.core.domain.model.NotificationScheduleConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationScheduleTimeService {
    
    private final NotificationScheduleConfigService configService;
    
    public boolean isNotificationAllowedNow() {
        return configService.isNotificationAllowed();
    }
    
    public LocalDateTime calculateNextAllowedDateTime() {
        Optional<NotificationScheduleConfigResponseDTO> configOpt = configService.getActiveConfig();
        
        if (configOpt.isEmpty()) {
            return LocalDateTime.now().plusMinutes(1);
        }
        
        NotificationScheduleConfigResponseDTO config = configOpt.get();
        if (!config.isAtivo()) {
            return LocalDateTime.now().plusDays(1);
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek currentDay = now.getDayOfWeek();
        
        List<NotificationScheduleConfig.DayOfWeek> allowedDays = config.getDiasDaSemana();
        LocalTime startTime = config.getHoraInicio();
        LocalTime endTime = config.getHoraFim();
        
        LocalDateTime nextAllowed = findNextAllowedDateTime(now, allowedDays, startTime, endTime);
        
        return nextAllowed;
    }
    
    public long getMinutesUntilNextAllowedTime() {
        LocalDateTime nextAllowed = calculateNextAllowedDateTime();
        LocalDateTime now = LocalDateTime.now();
        
        return java.time.Duration.between(now, nextAllowed).toMinutes();
    }
    
    private LocalDateTime findNextAllowedDateTime(LocalDateTime from, 
                                                List<NotificationScheduleConfig.DayOfWeek> allowedDays,
                                                LocalTime startTime, 
                                                LocalTime endTime) {
        
        LocalDateTime current = from;
        
        for (int i = 0; i < 7; i++) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            NotificationScheduleConfig.DayOfWeek configDay = mapToConfigDayOfWeek(dayOfWeek);
            
            if (allowedDays.contains(configDay)) {
                LocalTime currentTime = current.toLocalTime();
                
                if (currentTime.isBefore(startTime)) {
                    return current.toLocalDate().atTime(startTime);
                } else if (!currentTime.isAfter(endTime)) {
                    return current;
                } else {
                    LocalDateTime nextDay = current.plusDays(1).toLocalDate().atTime(startTime);
                    return findNextAllowedDateTime(nextDay, allowedDays, startTime, endTime);
                }
            } else {
                current = current.plusDays(1).toLocalDate().atTime(startTime);
            }
        }
        
        return current;
    }
    
    private NotificationScheduleConfig.DayOfWeek mapToConfigDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> NotificationScheduleConfig.DayOfWeek.SEGUNDA_FEIRA;
            case TUESDAY -> NotificationScheduleConfig.DayOfWeek.TERCA_FEIRA;
            case WEDNESDAY -> NotificationScheduleConfig.DayOfWeek.QUARTA_FEIRA;
            case THURSDAY -> NotificationScheduleConfig.DayOfWeek.QUINTA_FEIRA;
            case FRIDAY -> NotificationScheduleConfig.DayOfWeek.SEXTA_FEIRA;
            case SATURDAY -> NotificationScheduleConfig.DayOfWeek.SABADO;
            case SUNDAY -> NotificationScheduleConfig.DayOfWeek.DOMINGO;
        };
    }
}
