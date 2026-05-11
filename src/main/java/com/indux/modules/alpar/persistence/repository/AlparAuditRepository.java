package com.indux.modules.alpar.persistence.repository;

import com.indux.modules.alpar.persistence.model.AlparAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlparAuditRepository extends MongoRepository<AlparAuditLog, String> {
}
