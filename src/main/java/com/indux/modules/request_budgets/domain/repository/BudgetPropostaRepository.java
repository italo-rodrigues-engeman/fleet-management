package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetProposta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetPropostaRepository extends MongoRepository<BudgetProposta, String> {
    List<BudgetProposta> findByVersionId(String versionId);
}

