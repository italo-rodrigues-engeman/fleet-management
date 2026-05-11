package com.indux.modules.advance_suppliers.domain.repository;

import com.indux.modules.advance_suppliers.domain.entities.log.AdvanceSupplierLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvanceSuppliersLogRepository extends MongoRepository<AdvanceSupplierLog, String> {
}
