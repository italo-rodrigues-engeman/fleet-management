package com.indux.modules.ocf.domain.repository.log;

import com.indux.modules.ocf.domain.model.log.FinanceOccurrenceLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FinanceOccurrenceLogRepository extends MongoRepository<FinanceOccurrenceLog, UUID> {
    List<FinanceOccurrenceLog> findAllByOccurrenceId(String id);
}
