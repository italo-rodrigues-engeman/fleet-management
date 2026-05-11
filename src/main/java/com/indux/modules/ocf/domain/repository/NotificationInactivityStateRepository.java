package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.NotificationInactivityState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationInactivityStateRepository extends MongoRepository<NotificationInactivityState, String> {
    
    List<NotificationInactivityState> findByProcessadoFalse();
    
    Optional<NotificationInactivityState> findByConfigIdAndProcessadoFalse(String configId);
    
    List<NotificationInactivityState> findByInicioInatividadeBeforeAndProcessadoFalse(LocalDateTime before);
}
