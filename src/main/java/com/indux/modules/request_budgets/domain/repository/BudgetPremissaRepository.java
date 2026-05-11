package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetPremissa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetPremissaRepository extends MongoRepository<BudgetPremissa, String> {
    List<BudgetPremissa> findByVersionId(String versionId);
}

