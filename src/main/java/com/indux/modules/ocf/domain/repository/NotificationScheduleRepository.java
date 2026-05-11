package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.NotificationSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationScheduleRepository extends MongoRepository<NotificationSchedule, String> {
    
    /**
     * Busca agendamento por canal, etapa e userIds
     */
    Optional<NotificationSchedule> findByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrue(String canal, Integer etapa, String userId);
    
    /**
     * Busca todos os agendamentos ativos por canal
     */
    List<NotificationSchedule> findByCanaisContainingAndAtivoTrue(String canal);
    
    /**
     * Busca todos os agendamentos ativos por etapa
     */
    List<NotificationSchedule> findByEtapaAndAtivoTrue(Integer etapa);
    
    /**
     * Busca todos os agendamentos ativos por userId
     */
    List<NotificationSchedule> findByUserIdsContainingAndAtivoTrue(String userId);
    
    /**
     * Busca todos os agendamentos ativos
     */
    List<NotificationSchedule> findByAtivoTrue();
    
    /**
     * Verifica se existe agendamento para o canal, etapa e userIds
     */
    boolean existsByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrue(String canal, Integer etapa, String userId);
    
    /**
     * Busca agendamento por canal, etapa, userIds e ID (para atualização)
     */
    Optional<NotificationSchedule> findByCanaisContainingAndEtapaAndUserIdsContainingAndAtivoTrueAndIdNot(String canal, Integer etapa, String userId, String id);
    
    /**
     * Busca todos os agendamentos (ativos e inativos) por canal
     */
    List<NotificationSchedule> findByCanaisContaining(String canal);
    
    /**
     * Busca todos os agendamentos (ativos e inativos) por etapa
     */
    List<NotificationSchedule> findByEtapa(Integer etapa);
    
    /**
     * Busca todos os agendamentos (ativos e inativos) por userId
     */
    List<NotificationSchedule> findByUserIdsContaining(String userId);
}
