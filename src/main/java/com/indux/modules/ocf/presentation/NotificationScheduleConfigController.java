package com.indux.modules.ocf.presentation;

import com.indux.modules.ocf.application.dto.CreateNotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.dto.NotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.dto.UpdateNotificationScheduleConfigDTO;
import com.indux.modules.ocf.application.mapper.OcfNotificationScheduleConfigMapper;
import com.indux.modules.ocf.application.service.OcfNotificationScheduleConfigService;
import com.indux.modules.ocf.domain.model.NotificationScheduleConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ocf/notification-schedule-configs")
@RequiredArgsConstructor
public class NotificationScheduleConfigController {
    
    private final OcfNotificationScheduleConfigService configService;
    private final OcfNotificationScheduleConfigMapper mapper;
    
    @GetMapping
    public ResponseEntity<List<NotificationScheduleConfigDTO>> findAll() {
        List<NotificationScheduleConfig> configs = configService.findAll();
        List<NotificationScheduleConfigDTO> dtos = configs.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<NotificationScheduleConfigDTO>> findActiveConfigs() {
        List<NotificationScheduleConfig> configs = configService.findActiveConfigs();
        List<NotificationScheduleConfigDTO> dtos = configs.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationScheduleConfigDTO> findById(@PathVariable String id) {
        return configService.findById(id)
                .map(config -> ResponseEntity.ok(mapper.toDTO(config)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<NotificationScheduleConfigDTO> create(
            @Valid @RequestBody CreateNotificationScheduleConfigDTO dto,
            JwtAuthenticationToken jwt) {
        
        String userId = jwt.getName();
        NotificationScheduleConfig config = mapper.toEntity(dto, userId);
        NotificationScheduleConfig saved = configService.create(config);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NotificationScheduleConfigDTO> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateNotificationScheduleConfigDTO dto,
            JwtAuthenticationToken jwt) {
        
        try {
            String userId = jwt.getName();
            NotificationScheduleConfig config = mapper.toEntity(dto, userId);
            NotificationScheduleConfig updated = configService.update(id, config);
            return ResponseEntity.ok(mapper.toDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        configService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<NotificationScheduleConfigDTO> activate(@PathVariable String id) {
        configService.activate(id);
        return configService.findById(id)
                .map(config -> ResponseEntity.ok(mapper.toDTO(config)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<NotificationScheduleConfigDTO> deactivate(@PathVariable String id) {
        configService.deactivate(id);
        return configService.findById(id)
                .map(config -> ResponseEntity.ok(mapper.toDTO(config)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/check-enabled")
    public ResponseEntity<Boolean> isNotificationEnabled() {
        boolean enabled = configService.isNotificationEnabled();
        return ResponseEntity.ok(enabled);
    }
    
    @PostMapping("/remove-duplicates")
    public ResponseEntity<String> removeDuplicates() {
        configService.removeDuplicateConfigurations();
        return ResponseEntity.ok("Configurações duplicadas removidas com sucesso");
    }
}
