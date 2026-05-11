package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetKeyword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetKeywordRepository extends MongoRepository<BudgetKeyword, String> {
    Optional<BudgetKeyword> findByValue(String value);
    List<BudgetKeyword> findByValueIn(Collection<String> values);
}


