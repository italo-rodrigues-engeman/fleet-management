package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.BudgetEsclarecimento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetEsclarecimentoRepository extends MongoRepository<BudgetEsclarecimento, String> {
    List<BudgetEsclarecimento> findByVersionId(String versionId);
}

