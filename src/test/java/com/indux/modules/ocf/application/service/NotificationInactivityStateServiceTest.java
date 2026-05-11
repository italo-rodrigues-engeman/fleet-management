package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.domain.model.NotificationInactivityState;
import com.indux.modules.ocf.domain.repository.NotificationInactivityStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationInactivityStateServiceTest {
    
    @Mock
    private NotificationInactivityStateRepository repository;
    
    @InjectMocks
    private OcfNotificationScheduleConfigService service;
    
    private NotificationInactivityState state;
    
    @BeforeEach
    void setUp() {
        state = NotificationInactivityState.builder()
                .id("1")
                .configId("config-1")
                .inicioInatividade(LocalDateTime.now().minusHours(2))
                .fimInatividade(LocalDateTime.now())
                .processado(false)
                .criadoEm(LocalDateTime.now().minusHours(2))
                .build();
    }
    
    @Test
    void getPendingInactivityStates_ShouldReturnUnprocessedStates() {
        when(repository.findByProcessadoFalse()).thenReturn(Arrays.asList(state));
        
        List<NotificationInactivityState> result = service.getPendingInactivityStates();
        
        assertEquals(1, result.size());
        assertEquals(state.getId(), result.get(0).getId());
        assertFalse(result.get(0).getProcessado());
        verify(repository).findByProcessadoFalse();
    }
    
    @Test
    void markInactivityStateAsProcessed_ShouldMarkAsProcessed() {
        when(repository.findById("1")).thenReturn(Optional.of(state));
        when(repository.save(any(NotificationInactivityState.class))).thenReturn(state);
        
        service.markInactivityStateAsProcessed("1");
        
        assertTrue(state.getProcessado());
        assertNotNull(state.getProcessadoEm());
        verify(repository).findById("1");
        verify(repository).save(state);
    }
    
    @Test
    void markInactivityStateAsProcessed_ShouldHandleNotFoundState() {
        when(repository.findById("1")).thenReturn(Optional.empty());
        
        // Não deve lançar exceção
        service.markInactivityStateAsProcessed("1");
        
        verify(repository).findById("1");
        verify(repository, never()).save(any());
    }
}
