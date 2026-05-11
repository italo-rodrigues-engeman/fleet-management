package com.indux.core.domain.repository;

import com.indux.core.domain.model.NotificationScheduleConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationScheduleConfigRepository extends MongoRepository<NotificationScheduleConfig, String> {
    
    Optional<NotificationScheduleConfig> findByAtivoTrue();
    
    boolean existsByAtivoTrue();
}
