package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetCaracteristica;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetCaracteristicaRepository extends MongoRepository<BudgetCaracteristica, String> {
    List<BudgetCaracteristica> findByIdIn(List<String> ids);
}

