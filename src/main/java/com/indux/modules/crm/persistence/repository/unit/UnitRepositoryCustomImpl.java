package com.indux.modules.crm.persistence.repository.unit;

import com.indux.modules.crm.application.dto.filter.UnitFilter;
import com.indux.modules.crm.application.dto.filter.UnitFilterAll;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.model.UnitModel;
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
public class UnitRepositoryCustomImpl implements UnitRepositoryCustom{

    private final MongoTemplate mongoTemplate;

    public UnitRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private Criteria buildCriteria(UnitFilter filter, String companyId) {
        List<Criteria> criteria = new ArrayList<>();

        if (companyId != null && !companyId.trim().isEmpty()) {
            criteria.add(Criteria.where("company").is(companyId));
        }

        if (filter.type() != null) {
            criteria.add(Criteria.where("type").is(filter.type()));
        }

        if (filter.city() != null) {
            criteria.add(Criteria.where("city").is(filter.city()));
        }

        if (filter.state() != null) {
            criteria.add(Criteria.where("state").is(filter.state()));
        }

        if (filter.id() != null) {
            criteria.add(Criteria.where("id").is(filter.id()));
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<UnitModel> filter(UnitFilter filter, String companyId, Pageable pageable) {
        Criteria criteria = buildCriteria(filter, companyId);

        Query query = new Query(criteria);
        query.with(pageable);

        List<UnitModel> units = mongoTemplate.find(
                query,
                UnitModel.class
        );

        long total = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                UnitModel.class
        );

        return new PageImpl<>(units, pageable, total);
    }

    private Criteria buildCriteriaAll(UnitFilterAll filter) {
        List<Criteria> criteria = new ArrayList<>();

        if (filter.client() != null && !filter.client().trim().isEmpty()) {
            criteria.add(Criteria.where("company").is(filter.client()));
        }

        if (filter.name() != null && !filter.name().trim().isEmpty()) {
            criteria.add(Criteria.where("name").regex(filter.name(), "i"));
        }

        if (filter.type() != null) {
            criteria.add(Criteria.where("type").is(filter.type()));
        }

        if (filter.city() != null) {
            criteria.add(Criteria.where("city").is(filter.city()));
        }

        if (filter.state() != null) {
            criteria.add(Criteria.where("state").is(filter.state()));
        }

        if (filter.status() != null) {
            criteria.add(Criteria.where("status").is(filter.status()));
        }

        if (filter.dataInicio() != null || filter.dataFim() != null) {
            Criteria dateCriteria = Criteria.where("registrationDate");
            if (filter.dataInicio() != null) {
                java.util.Date startDate = java.util.Date.from(filter.dataInicio().atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant());
                dateCriteria = dateCriteria.gte(startDate);
            }
            if (filter.dataFim() != null) {
                java.util.Date endDate = java.util.Date.from(filter.dataFim().atTime(23, 59, 59, 999000000).atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant());
                dateCriteria = dateCriteria.lte(endDate);
            }
            criteria.add(dateCriteria);
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<UnitModel> filterAll(UnitFilterAll filter, Pageable pageable) {
        Criteria criteria = buildCriteriaAll(filter);

        Query query = new Query(criteria);
        query.with(pageable);

        List<UnitModel> units = mongoTemplate.find(
                query,
                UnitModel.class
        );

        long total = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                UnitModel.class
        );

        return new PageImpl<>(units, pageable, total);
    }
}
