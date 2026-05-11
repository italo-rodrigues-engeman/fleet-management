package com.indux.modules.ocf.infra;

import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.user.UserService;
import com.indux.modules.ocf.application.service.OcfNotificationScheduleConfigService;
import com.indux.modules.ocf.domain.model.NotificationInactivityState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationScheduleSchedulerAccumulatedTest {
    
    
    @Mock
    private MongoTemplate mongoTemplate;
    
    @Mock
    private ModuleManagementService moduleService;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private RegionalRepository regionalRepository;
    
    @Mock
    private OcfNotificationScheduleConfigService configService;
    
    @InjectMocks
    private NotificationScheduleScheduler scheduler;
    
    private NotificationInactivityState inactivityState;
    
    @BeforeEach
    void setUp() {
        inactivityState = NotificationInactivityState.builder()
                .id("1")
                .configId("config-1")
                .inicioInatividade(LocalDateTime.now().minusHours(2))
                .fimInatividade(LocalDateTime.now())
                .processado(false)
                .criadoEm(LocalDateTime.now().minusHours(2))
                .build();
        
        // schedule = new NotificationSchedule(); // Classe não implementada ainda
    }
    
    @Test
    void processPendingInactivityStates_ShouldProcessPendingStates() {
        when(configService.isNotificationEnabled()).thenReturn(true);
        when(configService.getPendingInactivityStates()).thenReturn(Arrays.asList(inactivityState));
        // when(notificationScheduleRepository.findByAtivoTrue()).thenReturn(Arrays.asList(schedule)); // Classe não implementada ainda
        
        scheduler.processNotificationSchedules();
        
        verify(configService).isNotificationEnabled();
        verify(configService).getPendingInactivityStates();
        verify(configService).markInactivityStateAsProcessed(inactivityState.getId());
    }
    
    @Test
    void processPendingInactivityStates_ShouldSkipStatesWithoutEndTime() {
        inactivityState.setFimInatividade(null);
        when(configService.isNotificationEnabled()).thenReturn(true);
        when(configService.getPendingInactivityStates()).thenReturn(Arrays.asList(inactivityState));
        // when(notificationScheduleRepository.findByAtivoTrue()).thenReturn(Arrays.asList(schedule)); // Classe não implementada ainda
        
        scheduler.processNotificationSchedules();
        
        verify(configService).isNotificationEnabled();
        verify(configService).getPendingInactivityStates();
        verify(configService, never()).markInactivityStateAsProcessed(any());
    }
    
    @Test
    void processPendingInactivityStates_ShouldHandleEmptyStates() {
        when(configService.isNotificationEnabled()).thenReturn(true);
        when(configService.getPendingInactivityStates()).thenReturn(Arrays.asList());
        // when(notificationScheduleRepository.findByAtivoTrue()).thenReturn(Arrays.asList(schedule)); // Classe não implementada ainda
        
        // Não deve lançar exceção
        scheduler.processNotificationSchedules();
        
        verify(configService).isNotificationEnabled();
        verify(configService).getPendingInactivityStates();
        verify(configService, never()).markInactivityStateAsProcessed(any());
    }
    
    @Test
    void processPendingInactivityStates_ShouldHandleException() {
        when(configService.isNotificationEnabled()).thenReturn(true);
        when(configService.getPendingInactivityStates()).thenThrow(new RuntimeException("Test exception"));
        
        // Não deve lançar exceção
        scheduler.processNotificationSchedules();
        
        verify(configService).isNotificationEnabled();
        verify(configService).getPendingInactivityStates();
    }
}
