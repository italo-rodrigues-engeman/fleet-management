package com.indux.modules.cdi.aplication.repository;

import com.indux.modules.cdi.aplication.dtos.FilterCdiTranslateDTO;
import com.indux.modules.cdi.domain.entities.models.Stage;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;
import com.indux.modules.cdi.domain.repositories.mongo.CdiRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CdiRepositoryImpl implements CdiRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public CdiRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<CdiEntity> getWithFilter(FilterCdiTranslateDTO filter, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter.type() != null) {
            criteriaList.add(Criteria.where("type").is(filter.type().name()));
        }

        if (filter.scope() != null) {
            criteriaList.add(Criteria.where("scope").is(filter.scope().name()));
        }

        if (filter.status() != null) {
            criteriaList.add(Criteria.where("status").is(filter.status().name()));
        }

        if (filter.branches() != null && filter.branches().length > 0) {
            criteriaList.add(Criteria.where("applicant.branch").in(filter.branches()));
        }

        if (filter.stage() != null) {
            criteriaList.add(Criteria.where("stage").is(filter.stage().name()));
        }else{
            criteriaList.add(Criteria.where("stage").ne(Stage.CONCLUIDO));
        }

        Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        Query query = Query.query(criteria).with(pageable);

        query.with(Sort.by(Sort.Order.asc("statusOrder")));
        query.with(Sort.by(Sort.Order.desc("points")));
        query.with(Sort.by(Sort.Order.asc("createAt")));

        Query countQuery = Query.query(criteria);
        long totalRows = mongoTemplate.count(countQuery, CdiEntity.class);

        var resultList = mongoTemplate.find(query, CdiEntity.class);

        return new PageImpl<>(resultList, pageable, totalRows);
    }
}