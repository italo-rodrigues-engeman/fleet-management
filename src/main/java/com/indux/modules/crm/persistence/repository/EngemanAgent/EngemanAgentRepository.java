package com.indux.modules.crm.persistence.repository.EngemanAgent;

import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EngemanAgentRepository extends MongoRepository<EngemanAgentModel, String>, EngemanAgentRepositoryCustom {

    Optional<EngemanAgentModel> findByCpf(String cpf);
}
