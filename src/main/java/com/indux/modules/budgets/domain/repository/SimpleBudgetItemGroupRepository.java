package com.indux.modules.budgets.domain.repository;

import com.indux.modules.budgets.domain.model.SimpleBudgetItemGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimpleBudgetItemGroupRepository extends MongoRepository<SimpleBudgetItemGroup, String> {
    Optional<SimpleBudgetItemGroup> findTopByOrderBySequentialIdDesc();
    boolean existsByName(String name);
}
