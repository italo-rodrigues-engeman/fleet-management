package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetCirculares;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetCircularesRepository extends MongoRepository<BudgetCirculares, String> {
    List<BudgetCirculares> findByVersionId(String versionId);
}

