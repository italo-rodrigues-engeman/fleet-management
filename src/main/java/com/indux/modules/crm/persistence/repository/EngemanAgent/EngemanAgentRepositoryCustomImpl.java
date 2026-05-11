package com.indux.modules.crm.persistence.repository.EngemanAgent;

import com.indux.modules.crm.application.dto.filter.EngemanAgentFilter;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EngemanAgentRepositoryCustomImpl implements EngemanAgentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public EngemanAgentRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private Criteria buildCriteria(EngemanAgentFilter filter) {
        List<Criteria> criteria = new ArrayList<>();

        if (filter.name() != null) {
            criteria.add(Criteria.where("name").is(filter.name()));
        }

        if (filter.cpf() != null) {
            criteria.add(Criteria.where("cpf").is(filter.cpf()));
        }

        if (filter.status() != null) {
            criteria.add(Criteria.where("status").is(filter.status()));
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<EngemanAgentModel> filter(EngemanAgentFilter filter, Pageable pageable) {
        Criteria criteria = buildCriteria(filter);

        Query query = new Query(criteria);
        query.with(pageable);

        List<EngemanAgentModel> agents = mongoTemplate.find(
                query,
                EngemanAgentModel.class
        );

        long total = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                CompanyModel.class
        );

        return new PageImpl<>(agents, pageable, total);
    }
}
