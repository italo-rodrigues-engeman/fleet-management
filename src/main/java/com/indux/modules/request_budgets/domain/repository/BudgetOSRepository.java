package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetOS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetOSRepository extends MongoRepository<BudgetOS, String> {
    List<BudgetOS> findByBudgetId(String budgetId);
}

