package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.NotificationScheduleConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("ocfNotificationScheduleConfigRepository")
public interface OcfNotificationScheduleConfigRepository extends MongoRepository<NotificationScheduleConfig, String> {
    
    List<NotificationScheduleConfig> findByAtivoTrue();
    
    List<NotificationScheduleConfig> findAll(Sort sort);
    
    @Query("{ 'ativo': true, 'diaDaSemana': ?0, 'horaInicio': { $lte: ?1 }, 'horaFim': { $gte: ?1 } }")
    List<NotificationScheduleConfig> findActiveConfigsForCurrentTime(java.time.DayOfWeek dayOfWeek, java.time.LocalTime currentTime);
    
    Optional<NotificationScheduleConfig> findByDiaDaSemanaAndAtivoTrue(java.time.DayOfWeek diaDaSemana);
}
