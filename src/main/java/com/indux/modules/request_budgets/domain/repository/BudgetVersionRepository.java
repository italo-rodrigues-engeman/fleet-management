package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetVersionRepository extends MongoRepository<BudgetVersion, String> {
    List<BudgetVersion> findByBudgetId(String budgetId);
}



