package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.domain.model.NotificationInactivityState;
import com.indux.modules.ocf.domain.model.NotificationScheduleConfig;
import com.indux.modules.ocf.domain.repository.NotificationInactivityStateRepository;
import com.indux.modules.ocf.domain.repository.OcfNotificationScheduleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("ocfNotificationScheduleConfigService")
@RequiredArgsConstructor
@Transactional
public class OcfNotificationScheduleConfigService {
    
    private final OcfNotificationScheduleConfigRepository repository;
    private final NotificationInactivityStateRepository inactivityStateRepository;
    
    public List<NotificationScheduleConfig> findAll() {
        List<NotificationScheduleConfig> allConfigs = repository.findAll();
        
        // Ordenar manualmente por dia da semana (MONDAY=1, TUESDAY=2, ..., SUNDAY=7)
        allConfigs.sort((config1, config2) -> {
            DayOfWeek day1 = config1.getDiaDaSemana();
            DayOfWeek day2 = config2.getDiaDaSemana();
            
            if (day1 == null && day2 == null) return 0;
            if (day1 == null) return 1;
            if (day2 == null) return -1;
            
            return day1.getValue() - day2.getValue();
        });
        
        return allConfigs;
    }
    
    public List<NotificationScheduleConfig> findActiveConfigs() {
        return repository.findByAtivoTrue();
    }
    
    public Optional<NotificationScheduleConfig> findById(String id) {
        return repository.findById(id);
    }
    
    public NotificationScheduleConfig create(NotificationScheduleConfig config) {
        // Verificar se já existe configuração para este dia (ativa ou inativa)
        List<NotificationScheduleConfig> existingConfigs = repository.findAll().stream()
                .filter(c -> c.getDiaDaSemana() != null && c.getDiaDaSemana().equals(config.getDiaDaSemana()))
                .toList();
        
        if (!existingConfigs.isEmpty()) {
            // Se existe, atualizar a primeira configuração encontrada (mais recente)
            NotificationScheduleConfig existing = existingConfigs.get(0);
            existing.setHoraInicio(config.getHoraInicio());
            existing.setHoraFim(config.getHoraFim());
            existing.setAtivo(config.getAtivo());
            existing.setAtualizadoEm(LocalDateTime.now());
            existing.setAtualizadoPor(config.getCriadoPor());
            
            // Remover outras configurações duplicadas para o mesmo dia
            if (existingConfigs.size() > 1) {
                List<String> idsToDelete = existingConfigs.subList(1, existingConfigs.size())
                        .stream()
                        .map(NotificationScheduleConfig::getId)
                        .toList();
                repository.deleteAllById(idsToDelete);
            }
            
            return repository.save(existing);
        } else {
            // Se não existe, criar nova configuração
            config.setCriadoEm(LocalDateTime.now());
            return repository.save(config);
        }
    }
    
    public NotificationScheduleConfig update(String id, NotificationScheduleConfig config) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNome(config.getNome());
                    existing.setDescricao(config.getDescricao());
                    existing.setDiaDaSemana(config.getDiaDaSemana());
                    existing.setHoraInicio(config.getHoraInicio());
                    existing.setHoraFim(config.getHoraFim());
                    existing.setAtivo(config.getAtivo());
                    existing.setAtualizadoEm(LocalDateTime.now());
                    existing.setAtualizadoPor(config.getAtualizadoPor());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Configuração não encontrada com ID: " + id));
    }
    
    public void delete(String id) {
        repository.deleteById(id);
    }
    
    public void activate(String id) {
        repository.findById(id)
                .ifPresent(config -> {
                    config.setAtivo(true);
                    config.setAtualizadoEm(LocalDateTime.now());
                    repository.save(config);
                });
    }
    
    public void deactivate(String id) {
        repository.findById(id)
                .ifPresent(config -> {
                    config.setAtivo(false);
                    config.setAtualizadoEm(LocalDateTime.now());
                    repository.save(config);
                });
    }
    
    public boolean isNotificationEnabled() {
        List<NotificationScheduleConfig> activeConfigs = findActiveConfigs();
        
        if (activeConfigs.isEmpty()) {
            return false; // Se não há configurações, notificações estão desabilitadas
        }
        
        LocalDateTime agora = LocalDateTime.now();
        DayOfWeek diaAtual = agora.toLocalDate().getDayOfWeek();
        LocalTime horaAtual = agora.toLocalTime();
        
        boolean isEnabled = activeConfigs.stream()
                .anyMatch(config -> config.getDiaDaSemana().equals(diaAtual) &&
                        horaAtual.isAfter(config.getHoraInicio()) &&
                        horaAtual.isBefore(config.getHoraFim()));
        
        // Rastrear início e fim de períodos de atividade
        trackActivityState(activeConfigs, agora, isEnabled);
        
        return isEnabled;
    }
    
    private void trackActivityState(List<NotificationScheduleConfig> activeConfigs, LocalDateTime agora, boolean isCurrentlyEnabled) {
        try {
            for (NotificationScheduleConfig config : activeConfigs) {
                boolean configIsActive = config.getDiaDaSemana().equals(agora.toLocalDate().getDayOfWeek()) &&
                        agora.toLocalTime().isAfter(config.getHoraInicio()) &&
                        agora.toLocalTime().isBefore(config.getHoraFim());
                
                Optional<NotificationInactivityState> existingState = 
                        inactivityStateRepository.findByConfigIdAndProcessadoFalse(config.getId());
                
                if (configIsActive && isCurrentlyEnabled && existingState.isEmpty()) {
                    // Novo período de atividade iniciado (não precisamos rastrear mais)
                    // As notificações acumuladas não são mais necessárias
                } else if (!configIsActive && !isCurrentlyEnabled && existingState.isPresent()) {
                    // Período de atividade acabou, marcar fim
                    NotificationInactivityState state = existingState.get();
                    state.setFimInatividade(agora);
                    inactivityStateRepository.save(state);
                } else if (configIsActive && !isCurrentlyEnabled && existingState.isEmpty()) {
                    // Período de inatividade iniciado (fora do horário de atividade)
                    NotificationInactivityState newState = NotificationInactivityState.createInactivityStart(config.getId(), agora);
                    inactivityStateRepository.save(newState);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao rastrear estado de atividade", e);
        }
    }
    
    public List<NotificationInactivityState> getPendingInactivityStates() {
        return inactivityStateRepository.findByProcessadoFalse();
    }
    
    public void markInactivityStateAsProcessed(String stateId) {
        inactivityStateRepository.findById(stateId)
                .ifPresent(state -> {
                    state.setProcessado(true);
                    state.setProcessadoEm(LocalDateTime.now());
                    inactivityStateRepository.save(state);
                });
    }
    
    /**
     * Remove configurações duplicadas, mantendo apenas uma por dia da semana
     */
    public void removeDuplicateConfigurations() {
        List<NotificationScheduleConfig> allConfigs = repository.findAll();
        
        // Agrupar por dia da semana
        Map<DayOfWeek, List<NotificationScheduleConfig>> configsByDay = allConfigs.stream()
                .filter(config -> config.getDiaDaSemana() != null)
                .collect(Collectors.groupingBy(NotificationScheduleConfig::getDiaDaSemana));
        
        // Para cada dia, manter apenas a configuração mais recente
        for (Map.Entry<DayOfWeek, List<NotificationScheduleConfig>> entry : configsByDay.entrySet()) {
            List<NotificationScheduleConfig> dayConfigs = entry.getValue();
            
            if (dayConfigs.size() > 1) {
                // Ordenar por data de criação (mais recente primeiro)
                dayConfigs.sort((c1, c2) -> {
                    if (c1.getCriadoEm() == null && c2.getCriadoEm() == null) return 0;
                    if (c1.getCriadoEm() == null) return 1;
                    if (c2.getCriadoEm() == null) return -1;
                    return c2.getCriadoEm().compareTo(c1.getCriadoEm());
                });
                
                // Manter apenas a primeira (mais recente) e deletar as outras
                List<String> idsToDelete = dayConfigs.subList(1, dayConfigs.size())
                        .stream()
                        .map(NotificationScheduleConfig::getId)
                        .toList();
                
                if (!idsToDelete.isEmpty()) {
                    repository.deleteAllById(idsToDelete);
                    log.info("Removidas {} configurações duplicadas para o dia {}", idsToDelete.size(), entry.getKey());
                }
            }
        }
    }
}
