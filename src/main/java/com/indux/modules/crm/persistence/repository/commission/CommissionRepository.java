package com.indux.modules.crm.persistence.repository.commission;

import com.indux.modules.crm.persistence.model.CommissionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRepository extends MongoRepository<CommissionModel, String> {
}
