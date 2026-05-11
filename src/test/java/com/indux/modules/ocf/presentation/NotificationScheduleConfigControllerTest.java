package com.indux.modules.ocf.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.modules.ocf.application.dto.CreateNotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.dto.NotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.dto.UpdateNotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.mapper.OcfNotificationScheduleConfigMapper;
import com.indux.modules.ocf.application.service.OcfNotificationScheduleConfigService;
import com.indux.modules.ocf.domain.model.NotificationScheduleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationScheduleConfigControllerTest {
    
    private MockMvc mockMvc;
    private OcfNotificationScheduleConfigService configService;
    private OcfNotificationScheduleConfigMapper mapper;
    private NotificationScheduleConfigController controller;
    private JwtAuthenticationToken jwtToken;
    
    private ObjectMapper objectMapper;
    
    private NotificationScheduleConfig config;
    private NotificationScheduleConfigDTO configDTO;
    private CreateNotificationScheduleConfigDTO createDTO;
    private UpdateNotificationScheduleConfigDTO updateDTO;
    
    @BeforeEach
    void setUp() {
        // Configurar mocks
        configService = org.mockito.Mockito.mock(OcfNotificationScheduleConfigService.class);
        mapper = org.mockito.Mockito.mock(OcfNotificationScheduleConfigMapper.class);
        controller = new NotificationScheduleConfigController(configService, mapper);
        
        // Configurar MockMvc
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(controller)
                .build();
        
        // Configurar ObjectMapper com suporte a LocalTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        // Configurar mock do JWT token
        jwtToken = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
        org.mockito.Mockito.when(jwtToken.getName()).thenReturn("test-user");
        
        config = NotificationScheduleConfig.builder()
                .id("1")
                .diaDaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(18, 0))
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .criadoPor("test-user")
                .build();
        
        configDTO = NotificationScheduleConfigDTO.builder()
                .id("1")
                .diaDaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(18, 0))
                .ativo(true)
                .build();
        
        createDTO = CreateNotificationScheduleConfigDTO.builder()
                .diaDaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(18, 0))
                .ativo(true)
                .build();
        
        updateDTO = UpdateNotificationScheduleConfigDTO.builder()
                .diaDaSemana(DayOfWeek.WEDNESDAY)
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(17, 0))
                .ativo(false)
                .build();
    }
    
    @Test
    void findAll_ShouldReturnAllConfigs() throws Exception {
        List<NotificationScheduleConfig> configs = Arrays.asList(config);
        List<NotificationScheduleConfigDTO> dtos = Arrays.asList(configDTO);
        
        when(configService.findAll()).thenReturn(configs);
        when(mapper.toDTO(any(NotificationScheduleConfig.class))).thenReturn(configDTO);
        
        mockMvc.perform(get("/api/ocf/notification-schedule-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].id").value("1"));
    }
    
    @Test
    void findActiveConfigs_ShouldReturnOnlyActiveConfigs() throws Exception {
        List<NotificationScheduleConfig> configs = Arrays.asList(config);
        List<NotificationScheduleConfigDTO> dtos = Arrays.asList(configDTO);
        
        when(configService.findActiveConfigs()).thenReturn(configs);
        when(mapper.toDTO(any(NotificationScheduleConfig.class))).thenReturn(configDTO);
        
        mockMvc.perform(get("/api/ocf/notification-schedule-configs/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"));
    }
    
    @Test
    void findById_ShouldReturnConfigWhenExists() throws Exception {
        when(configService.findById("1")).thenReturn(Optional.of(config));
        when(mapper.toDTO(config)).thenReturn(configDTO);
        
        mockMvc.perform(get("/api/ocf/notification-schedule-configs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.id").value("1"));
    }
    
    @Test
    void findById_ShouldReturn404WhenNotExists() throws Exception {
        when(configService.findById("1")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/ocf/notification-schedule-configs/1"))
                .andExpect(status().isNotFound());
    }
    
    // @Test
    void create_ShouldCreateNewConfig() throws Exception {
        when(mapper.toEntity(any(CreateNotificationScheduleConfigDTO.class), eq("test-user")))
                .thenReturn(config);
        when(configService.create(config)).thenReturn(config);
        when(mapper.toDTO(config)).thenReturn(configDTO);
        
        mockMvc.perform(post("/api/ocf/notification-schedule-configs")
                        .header("user-id", "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));
    }
    
    // @Test
    void update_ShouldUpdateExistingConfig() throws Exception {
        when(mapper.toEntity(any(UpdateNotificationScheduleConfigDTO.class), eq("test-user")))
                .thenReturn(config);
        when(configService.update("1", config)).thenReturn(config);
        when(mapper.toDTO(config)).thenReturn(configDTO);
        
        mockMvc.perform(put("/api/ocf/notification-schedule-configs/1")
                        .header("user-id", "test-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }
    
    @Test
    void delete_ShouldDeleteConfig() throws Exception {
        mockMvc.perform(delete("/api/ocf/notification-schedule-configs/1"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void activate_ShouldActivateConfig() throws Exception {
        when(configService.findById("1")).thenReturn(Optional.of(config));
        when(mapper.toDTO(config)).thenReturn(configDTO);
        
        mockMvc.perform(put("/api/ocf/notification-schedule-configs/1/activate"))
                .andExpect(status().isOk());
    }
    
    @Test
    void deactivate_ShouldDeactivateConfig() throws Exception {
        when(configService.findById("1")).thenReturn(Optional.of(config));
        when(mapper.toDTO(config)).thenReturn(configDTO);
        
        mockMvc.perform(put("/api/ocf/notification-schedule-configs/1/deactivate"))
                .andExpect(status().isOk());
    }
    
    @Test
    void isNotificationEnabled_ShouldReturnStatus() throws Exception {
        when(configService.isNotificationEnabled()).thenReturn(false);
        
        mockMvc.perform(get("/api/ocf/notification-schedule-configs/check-enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
