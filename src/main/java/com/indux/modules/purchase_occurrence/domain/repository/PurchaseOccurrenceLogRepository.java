package com.indux.modules.purchase_occurrence.domain.repository;

import com.indux.modules.purchase_occurrence.domain.entities.log.PurchaseOccurrenceLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOccurrenceLogRepository extends MongoRepository<PurchaseOccurrenceLog, String> {
}
