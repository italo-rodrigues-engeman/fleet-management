package com.indux.modules.budgets.domain.repository;

import com.indux.modules.budgets.domain.model.SimpleBudget;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimpleBudgetRepository extends MongoRepository<SimpleBudget, String>, SimpleBudgetRepositoryCustom {
    Optional<SimpleBudget> findByNomeOportunidade(String nomeOportunidade);
    boolean existsByNomeOportunidade(String nomeOportunidade);
}

