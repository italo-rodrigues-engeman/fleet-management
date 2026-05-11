package com.indux.modules.crm.persistence.repository.commercialInteractions;

import com.indux.modules.crm.application.dto.filter.CommercialInteractionsFilter;
import com.indux.modules.crm.persistence.model.CommercialInteractionsModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Repository
public class CommercialInteractionsRepositoryCustomImpl implements CommercialInteractionsRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public CommercialInteractionsRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private Criteria buildCriteria(CommercialInteractionsFilter filter) {
        List<Criteria> criteria = new ArrayList<>();

        if (filter.company() != null) {
            criteria.add(Criteria.where("company").is(filter.company()));
        }

        if (filter.status() != null) {
            criteria.add(Criteria.where("status").is(filter.status()));
        }

        if (filter.dataInicio() != null || filter.dataFim() != null) {
            Criteria dateCriteria = Criteria.where("date");

            if (filter.dataInicio() != null) {
                dateCriteria = dateCriteria.gte(filter.dataInicio());
            }

            if (filter.dataFim() != null) {
                dateCriteria = dateCriteria.lte(filter.dataFim());
            }
            criteria.add(dateCriteria);
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<CommercialInteractionsModel> filter(CommercialInteractionsFilter filter, Pageable pageable) {
        Criteria criteria = buildCriteria(filter);

        Query query = new Query(criteria);
        query.with(pageable);

        List<CommercialInteractionsModel> interactions = mongoTemplate.find(
                query,
                CommercialInteractionsModel.class);

        long total = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                CommercialInteractionsModel.class);

        return new PageImpl<>(interactions, pageable, total);
    }
}