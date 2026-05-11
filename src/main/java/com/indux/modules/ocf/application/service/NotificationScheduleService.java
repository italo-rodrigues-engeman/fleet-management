package com.indux.modules.ocf.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ocf.application.dto.*;
import com.indux.modules.ocf.domain.model.NotificationSchedule;
import com.indux.modules.ocf.domain.repository.NotificationScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationScheduleService {
    
    private final NotificationScheduleRepository repository;
    private final UserService userService;
    
    public NotificationScheduleService(NotificationScheduleRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }
    
    /**
     * Cria um novo agendamento de notificação
     */
    public NotificationScheduleDTO createSchedule(CreateNotificationScheduleDTO dto, String userId) {
        // Verificar se já existe agendamento para o mesmo canal, etapa e userIds
        for (String canal : dto.canais()) {
            for (String userIdItem : dto.userIds()) {
                if (repository.existsByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrue(canal, dto.etapa(), userIdItem)) {
                    throw new ModuleFailure(
                        String.format("Já existe um agendamento ativo para canal %s - etapa %d - userId %s", 
                            canal, dto.etapa(), userIdItem)
                    );
                }
            }
        }
        
        // Validar tempo limite (máximo 168 horas = 7 dias)
        if (dto.tempoLimiteHoras() > 168.0) {
            throw new ModuleFailure("Tempo limite não pode ser superior a 168 horas (7 dias)");
        }
        
        // Validar tempo limite mínimo (mínimo 0.0167 horas = 1 minuto)
        if (dto.tempoLimiteHoras() < 0.0167) {
            throw new ModuleFailure("Tempo limite deve ser pelo menos 0.0167 horas (1 minuto)");
        }
        
        // Validar canais (devem ser tipos válidos)
        for (String canal : dto.canais()) {
            if (!isValidCanal(canal)) {
                throw new ModuleFailure("Canal inválido: " + canal + ". Use: EMAIL, WHATSAPP, TEAMS ou WEBSOCKET");
            }
        }
        
        // Validar texto da notificação
        if (dto.textoNotificacao() == null || dto.textoNotificacao().trim().isEmpty()) {
            throw new ModuleFailure("Texto da notificação é obrigatório e não pode estar vazio");
        }
        
        if (dto.textoNotificacao().length() > 1000) {
            throw new ModuleFailure("Texto da notificação não pode ter mais de 1000 caracteres");
        }
        
        NotificationSchedule entity = dto.toEntity(userId);
        NotificationSchedule saved = repository.save(entity);
        
        return NotificationScheduleDTO.fromEntity(saved);
    }
    
    /**
     * Atualiza um agendamento existente
     */
    public NotificationScheduleDTO updateSchedule(String id, UpdateNotificationScheduleDTO dto, String userId) {
        NotificationSchedule entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Agendamento não encontrado com ID: " + id));
        
        // Verificar se já existe outro agendamento ativo para o mesmo canal, etapa e userIds
        for (String canal : dto.canais()) {
            for (String userIdItem : dto.userIds()) {
                Optional<NotificationSchedule> existingSchedule = repository.findByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrueAndIdNot(
                        canal, dto.etapa(), userIdItem, id);
                
                if (existingSchedule.isPresent()) {
                    throw new ModuleFailure(
                        String.format("Já existe um agendamento ativo para canal %s - etapa %d - userId %s", 
                            canal, dto.etapa(), userIdItem)
                    );
                }
            }
        }
        
        // Validar tempo limite (máximo 168 horas = 7 dias)
        if (dto.tempoLimiteHoras() > 168.0) {
            throw new ModuleFailure("Tempo limite não pode ser superior a 168 horas (7 dias)");
        }
        
        // Validar tempo limite mínimo (mínimo 0.0167 horas = 1 minuto)
        if (dto.tempoLimiteHoras() < 0.0167) {
            throw new ModuleFailure("Tempo limite deve ser pelo menos 0.0167 horas (1 minuto)");
        }
        
        // Validar canais (devem ser tipos válidos)
        for (String canal : dto.canais()) {
            if (!isValidCanal(canal)) {
                throw new ModuleFailure("Canal inválido: " + canal + ". Use: EMAIL, WHATSAPP, TEAMS ou WEBSOCKET");
            }
        }
        
        // Validar texto da notificação
        if (dto.textoNotificacao() == null || dto.textoNotificacao().trim().isEmpty()) {
            throw new ModuleFailure("Texto da notificação é obrigatório e não pode estar vazio");
        }
        
        if (dto.textoNotificacao().length() > 1000) {
            throw new ModuleFailure("Texto da notificação não pode ter mais de 1000 caracteres");
        }
        
        dto.updateEntity(entity, userId);
        NotificationSchedule saved = repository.save(entity);
        
        return NotificationScheduleDTO.fromEntity(saved);
    }
    
    /**
     * Busca um agendamento por ID
     */
    public NotificationScheduleDTO getScheduleById(String id) {
        NotificationSchedule entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Agendamento não encontrado com ID: " + id));
        
        return NotificationScheduleDTO.fromEntity(entity);
    }
    
    /**
     * Busca agendamento por canal, etapa e userId
     */
    public Optional<NotificationScheduleDTO> getScheduleByCanalEtapaAndUserId(String canal, Integer etapa, String userId) {
        return repository.findByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrue(canal, etapa, userId)
                .map(NotificationScheduleDTO::fromEntity);
    }
    
    /**
     * Lista todos os agendamentos ativos
     */
    public List<NotificationScheduleDTO> getAllActiveSchedules() {
        return repository.findByAtivoTrue()
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista todos os agendamentos (ativos e inativos)
     */
    public List<NotificationScheduleDTO> getAllSchedules() {
        return repository.findAll()
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por canal
     */
    public List<NotificationScheduleDTO> getSchedulesByCanal(String canal) {
        return repository.findByCanaisContainingAndAtivoTrue(canal)
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por etapa
     */
    public List<NotificationScheduleDTO> getSchedulesByEtapa(Integer etapa) {
        return repository.findByEtapaAndAtivoTrue(etapa)
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por userId
     */
    public List<NotificationScheduleDTO> getSchedulesByUserId(String userId) {
        return repository.findByUserIdsContainingAndAtivoTrue(userId)
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por canal (ativos e inativos)
     */
    public List<NotificationScheduleDTO> getAllSchedulesByCanal(String canal) {
        return repository.findByCanaisContaining(canal)
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por etapa (ativos e inativos)
     */
    public List<NotificationScheduleDTO> getAllSchedulesByEtapa(Integer etapa) {
        return repository.findByEtapa(etapa)
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por userId (ativos e inativos)
     */
    public List<NotificationScheduleDTO> getAllSchedulesByUserId(String userId) {
        return repository.findByUserIdsContaining(userId)
                .stream()
                .map(NotificationScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Desativa um agendamento (soft delete)
     */
    public GenericMessage deactivateSchedule(String id, String userId) {
        NotificationSchedule entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Agendamento não encontrado com ID: " + id));
        
        entity.setAtivo(false);
        entity.setAtualizadoEm(LocalDateTime.now());
        entity.setAtualizadoPor(userId);
        
        repository.save(entity);
        
        return new GenericMessage("Agendamento desativado com sucesso.", 200);
    }
    
    /**
     * Ativa um agendamento
     */
    public GenericMessage activateSchedule(String id, String userId) {
        NotificationSchedule entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Agendamento não encontrado com ID: " + id));
        
        // Verificar se já existe agendamento ativo para o mesmo canal, etapa e userIds
        for (String canal : entity.getCanais()) {
            for (String userIdItem : entity.getUserIds()) {
                if (repository.existsByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrue(canal, entity.getEtapa(), userIdItem)) {
                    throw new ModuleFailure(
                        String.format("Já existe um agendamento ativo para canal %s - etapa %d - userId %s", 
                            canal, entity.getEtapa(), userIdItem)
                    );
                }
            }
        }
        
        entity.setAtivo(true);
        entity.setAtualizadoEm(LocalDateTime.now());
        entity.setAtualizadoPor(userId);
        
        repository.save(entity);
        
        return new GenericMessage("Agendamento ativado com sucesso.", 200);
    }
    
    /**
     * Remove um agendamento permanentemente
     */
    public GenericMessage deleteSchedule(String id) {
        if (!repository.existsById(id)) {
            throw new ModuleNotFoundFailure("Agendamento não encontrado com ID: " + id);
        }
        
        repository.deleteById(id);
        
        return new GenericMessage("Agendamento removido com sucesso.", 200);
    }
    
    /**
     * Busca um agendamento por ID com informações completas dos usuários
     */
    public NotificationScheduleWithUsersDTO getScheduleByIdWithUsers(String id) {
        NotificationSchedule entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Agendamento não encontrado com ID: " + id));
        
        List<SimpleUser> usuarios = buscarUsuariosCompletos(entity.getUserIds());
        
        return NotificationScheduleWithUsersDTO.fromEntity(entity, usuarios);
    }
    
    /**
     * Lista todos os agendamentos ativos com informações completas dos usuários
     */
    public List<NotificationScheduleWithUsersDTO> getAllActiveSchedulesWithUsers() {
        return repository.findByAtivoTrue()
                .stream()
                .map(entity -> {
                    List<SimpleUser> usuarios = buscarUsuariosCompletos(entity.getUserIds());
                    return NotificationScheduleWithUsersDTO.fromEntity(entity, usuarios);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por canal com informações completas dos usuários
     */
    public List<NotificationScheduleWithUsersDTO> getSchedulesByCanalWithUsers(String canal) {
        return repository.findByCanaisContainingAndAtivoTrue(canal)
                .stream()
                .map(entity -> {
                    List<SimpleUser> usuarios = buscarUsuariosCompletos(entity.getUserIds());
                    return NotificationScheduleWithUsersDTO.fromEntity(entity, usuarios);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por etapa com informações completas dos usuários
     */
    public List<NotificationScheduleWithUsersDTO> getSchedulesByEtapaWithUsers(Integer etapa) {
        return repository.findByEtapaAndAtivoTrue(etapa)
                .stream()
                .map(entity -> {
                    List<SimpleUser> usuarios = buscarUsuariosCompletos(entity.getUserIds());
                    return NotificationScheduleWithUsersDTO.fromEntity(entity, usuarios);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lista agendamentos por userId com informações completas dos usuários
     */
    public List<NotificationScheduleWithUsersDTO> getSchedulesByUserIdWithUsers(String userId) {
        return repository.findByUserIdsContainingAndAtivoTrue(userId)
                .stream()
                .map(entity -> {
                    List<SimpleUser> usuarios = buscarUsuariosCompletos(entity.getUserIds());
                    return NotificationScheduleWithUsersDTO.fromEntity(entity, usuarios);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Busca agendamento por canal, etapa e userId com informações completas dos usuários
     */
    public Optional<NotificationScheduleWithUsersDTO> getScheduleByCanalEtapaAndUserIdWithUsers(String canal, Integer etapa, String userId) {
        return repository.findByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrue(canal, etapa, userId)
                .map(entity -> {
                    List<SimpleUser> usuarios = buscarUsuariosCompletos(entity.getUserIds());
                    return NotificationScheduleWithUsersDTO.fromEntity(entity, usuarios);
                });
    }
    
    /**
     * Busca informações completas dos usuários pelos IDs
     */
    private List<SimpleUser> buscarUsuariosCompletos(List<String> userIds) {
        return userIds.stream()
                .map(userId -> userService.getUserById(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Valida se o canal é válido
     */
    private boolean isValidCanal(String canal) {
        return canal != null && (canal.equals("EMAIL") || canal.equals("WHATSAPP") || 
                                canal.equals("TEAMS") || canal.equals("WEBSOCKET"));
    }
    
    //------------ Métodos específicos para registro de canais de notificação ------------//
    
    /**
     * Registra canais de notificação para ocorrências criadas (etapa 1)
     */
    public NotificationScheduleDTO registerOccurrenceNotificationChannels(RegisterOccurrenceNotificationChannelsDTO dto, String userId) {
        CreateNotificationScheduleDTO createDto = dto.toCreateNotificationScheduleDTO();
        return createSchedule(createDto, userId);
    }
    
    /**
     * Registra canais de notificação para ocorrências que saem da primeira etapa
     */
    public NotificationScheduleDTO registerNextStepNotificationChannels(RegisterNextStepNotificationChannelsDTO dto, String userId) {
        CreateNotificationScheduleDTO createDto = dto.toCreateNotificationScheduleDTO();
        return createSchedule(createDto, userId);
    }
    
    /**
     * Busca agendamentos de notificação para ocorrências criadas (etapa 1)
     */
    public List<NotificationScheduleDTO> getOccurrenceCreationNotificationSchedules() {
        return getSchedulesByEtapa(1);
    }
    
    /**
     * Busca agendamentos de notificação para uma etapa específica (após a primeira)
     */
    public List<NotificationScheduleDTO> getNextStepNotificationSchedules(Integer etapa) {
        if (etapa <= 1) {
            throw new ModuleFailure("Etapa deve ser maior que 1 para notificações de próximos passos");
        }
        return getSchedulesByEtapa(etapa);
    }
    
    /**
     * Busca agendamentos de notificação para ocorrências criadas com informações completas dos usuários
     */
    public List<NotificationScheduleWithUsersDTO> getOccurrenceCreationNotificationSchedulesWithUsers() {
        return getSchedulesByEtapaWithUsers(1);
    }
    
    /**
     * Busca agendamentos de notificação para uma etapa específica com informações completas dos usuários
     */
    public List<NotificationScheduleWithUsersDTO> getNextStepNotificationSchedulesWithUsers(Integer etapa) {
        if (etapa <= 1) {
            throw new ModuleFailure("Etapa deve ser maior que 1 para notificações de próximos passos");
        }
        return getSchedulesByEtapaWithUsers(etapa);
    }
}
