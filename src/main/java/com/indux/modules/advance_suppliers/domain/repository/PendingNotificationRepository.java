package com.indux.modules.advance_suppliers.domain.repository;

import com.indux.modules.advance_suppliers.domain.entities.PendingNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PendingNotificationRepository extends MongoRepository<PendingNotification, String> {
    
    // Fixed method name with proper parameter
    List<PendingNotification> findByDueDateLessThanEqualAndNotifiedIsFalse();
    
    // Alternative using @Query for more explicit control
    @Query("{ 'dueDate': { $lte: ?0 }, 'notified': false }")
    List<PendingNotification> findPendingNotifications(Instant currentDate);
}