package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.domain.model.NotificationScheduleConfig;
import com.indux.modules.ocf.domain.repository.OcfNotificationScheduleConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcfNotificationScheduleConfigServiceTest {
    
    @Mock
    private OcfNotificationScheduleConfigRepository repository;
    
    @InjectMocks
    private OcfNotificationScheduleConfigService service;
    
    private NotificationScheduleConfig config;
    
    @BeforeEach
    void setUp() {
        config = NotificationScheduleConfig.builder()
                .id("1")
                .nome("Configuração de Teste")
                .descricao("Configuração para testes")
                .diaDaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(18, 0))
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .criadoPor("test-user")
                .build();
    }
    
    @Test
    void findAll_ShouldReturnAllConfigs() {
        when(repository.findAll()).thenReturn(Arrays.asList(config));
        
        List<NotificationScheduleConfig> result = service.findAll();
        
        assertEquals(1, result.size());
        assertEquals(config.getId(), result.get(0).getId());
        verify(repository).findAll();
    }
    
    @Test
    void findActiveConfigs_ShouldReturnOnlyActiveConfigs() {
        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(config));
        
        List<NotificationScheduleConfig> result = service.findActiveConfigs();
        
        assertEquals(1, result.size());
        assertEquals(config.getId(), result.get(0).getId());
        verify(repository).findByAtivoTrue();
    }
    
    @Test
    void findById_ShouldReturnConfigWhenExists() {
        when(repository.findById("1")).thenReturn(Optional.of(config));
        
        Optional<NotificationScheduleConfig> result = service.findById("1");
        
        assertTrue(result.isPresent());
        assertEquals(config.getId(), result.get().getId());
        verify(repository).findById("1");
    }
    
    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        when(repository.findById("1")).thenReturn(Optional.empty());
        
        Optional<NotificationScheduleConfig> result = service.findById("1");
        
        assertFalse(result.isPresent());
        verify(repository).findById("1");
    }
    
    @Test
    void create_ShouldSetCreatedAtAndSave() {
        when(repository.save(any(NotificationScheduleConfig.class))).thenReturn(config);
        
        NotificationScheduleConfig result = service.create(config);
        
        assertNotNull(result.getCriadoEm());
        verify(repository).save(config);
    }
    
    @Test
    void update_ShouldUpdateExistingConfig() {
        NotificationScheduleConfig updatedConfig = NotificationScheduleConfig.builder()
                .id("1")
                .diaDaSemana(DayOfWeek.WEDNESDAY)
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(17, 0))
                .ativo(false)
                .atualizadoPor("test-user")
                .atualizadoEm(LocalDateTime.now())
                .build();
        
        when(repository.findById("1")).thenReturn(Optional.of(config));
        when(repository.save(any(NotificationScheduleConfig.class))).thenReturn(updatedConfig);
        
        NotificationScheduleConfig result = service.update("1", updatedConfig);
        
        assertEquals(DayOfWeek.WEDNESDAY, result.getDiaDaSemana());
        assertNotNull(result.getAtualizadoEm());
        verify(repository).findById("1");
        verify(repository).save(any(NotificationScheduleConfig.class));
    }
    
    @Test
    void update_ShouldThrowExceptionWhenConfigNotFound() {
        when(repository.findById("1")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> service.update("1", config));
        verify(repository).findById("1");
        verify(repository, never()).save(any());
    }
    
    @Test
    void delete_ShouldDeleteConfig() {
        service.delete("1");
        
        verify(repository).deleteById("1");
    }
    
    @Test
    void activate_ShouldSetActiveToTrue() {
        config.setAtivo(false);
        when(repository.findById("1")).thenReturn(Optional.of(config));
        when(repository.save(any(NotificationScheduleConfig.class))).thenReturn(config);
        
        service.activate("1");
        
        assertTrue(config.getAtivo());
        assertNotNull(config.getAtualizadoEm());
        verify(repository).findById("1");
        verify(repository).save(config);
    }
    
    @Test
    void deactivate_ShouldSetActiveToFalse() {
        when(repository.findById("1")).thenReturn(Optional.of(config));
        when(repository.save(any(NotificationScheduleConfig.class))).thenReturn(config);
        
        service.deactivate("1");
        
        assertFalse(config.getAtivo());
        assertNotNull(config.getAtualizadoEm());
        verify(repository).findById("1");
        verify(repository).save(config);
    }
    
    @Test
    void isNotificationEnabled_ShouldReturnFalseWhenNoActiveConfigs() {
        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList());
        
        boolean result = service.isNotificationEnabled();
        
        assertFalse(result);
        verify(repository).findByAtivoTrue();
    }
}
