package com.indux.modules.budgets.infra.mongo;

import com.indux.modules.budgets.application.dto.SimpleBudgetFilter;
import com.indux.modules.budgets.application.dto.SimpleBudgetSummaryDTO;
import com.indux.modules.budgets.application.mapper.SimpleBudgetMapper;
import com.indux.modules.budgets.domain.model.SimpleBudget;
import com.indux.modules.budgets.domain.repository.SimpleBudgetRepositoryCustom;
import com.indux.modules.budgets.domain.specification.SimpleBudgetCriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SimpleBudgetRepositoryImpl implements SimpleBudgetRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<SimpleBudget> search(SimpleBudgetFilter filter, Pageable pageable) {
        Query query = new Query().with(pageable);

        SimpleBudgetCriteriaBuilder.fromFilter(filter)
                .ifPresent(query::addCriteria);

        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), SimpleBudget.class);
        List<SimpleBudget> budgets = mongoTemplate.find(query, SimpleBudget.class);

        return new PageImpl<>(budgets, pageable, count);
    }
}
