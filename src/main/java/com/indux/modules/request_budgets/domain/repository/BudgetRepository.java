package com.indux.modules.request_budgets.domain.repository;

import com.indux.modules.request_budgets.domain.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {
    Optional<Budget> findByNomeOportunidade(String nomeOportunidade);
    boolean existsByNomeOportunidade(String nomeOportunidade);
}

