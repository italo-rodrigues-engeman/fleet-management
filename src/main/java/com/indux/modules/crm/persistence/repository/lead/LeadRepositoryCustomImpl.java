package com.indux.modules.crm.persistence.repository.lead;

import com.indux.modules.crm.application.dto.filter.LeadFilter;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.model.LeadModel;
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
import java.util.stream.Collectors;

@Repository
public class LeadRepositoryCustomImpl implements LeadRepositoryCustom{

    private final MongoTemplate mongoTemplate;

    public LeadRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Criteria buildCriteria(LeadFilter filter) {
        List<Criteria> criteria = new ArrayList<>();

        if (filter.name() != null) {
            criteria.add(Criteria.where("name").is(filter.name()));
        }

        if (filter.function() != null) {
            criteria.add(Criteria.where("function").is(filter.function()));
        }

        if (filter.decisionMakeLevel() != null) {
            criteria.add(Criteria.where("decisionMakeLevel").is(filter.decisionMakeLevel()));
        }

        if (filter.company() != null) {
            criteria.add(Criteria.where("company").is(filter.company()));
        }

        if (filter.status() != null) {
            criteria.add(Criteria.where("status").is(filter.status()));
        }

        if (filter.dataInicio() != null || filter.dataFim() != null) {
            Criteria dateCriteria = Criteria.where("registrationDate");
            if (filter.dataInicio() != null) {
                Date startDate = Date.from(filter.dataInicio().atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant());
                dateCriteria = dateCriteria.gte(startDate);
            }
            if (filter.dataFim() != null) {
                Date endDate = Date.from(filter.dataFim().atTime(23, 59, 59, 999000000).atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant());
                dateCriteria = dateCriteria.lte(endDate);
            }
            criteria.add(dateCriteria);
        }

        List<Criteria> unitCriteriaList = new ArrayList<>();
        
        if (filter.state() != null) {
            unitCriteriaList.add(Criteria.where("state").is(filter.state()));
        }

        if (filter.city() != null) {
            unitCriteriaList.add(Criteria.where("city").is(filter.city()));
        }

        if (filter.unit() != null) {
            unitCriteriaList.add(Criteria.where("_id").is(filter.unit()));
        }
        
        if (!unitCriteriaList.isEmpty()) {
            Criteria unitCriteria = new Criteria().andOperator(unitCriteriaList.toArray(new Criteria[0]));
            Query unitQuery = new Query(unitCriteria);
            
            List<String> validUnitIds = mongoTemplate.find(unitQuery, com.indux.modules.crm.persistence.model.UnitModel.class)
                    .stream()
                    .map(com.indux.modules.crm.persistence.model.UnitModel::getId)
                    .collect(Collectors.toList());

            if (validUnitIds.isEmpty()) {
                criteria.add(Criteria.where("unit").in("non_existent_id_to_return_empty"));
            } else {
                criteria.add(Criteria.where("unit").in(validUnitIds));
            }
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<LeadModel> filter(Pageable pageable, LeadFilter filter) {
        Criteria criteria = buildCriteria(filter);

        Query query = new Query(criteria);
        query.with(pageable);

        List<LeadModel> leads = mongoTemplate.find(
                query,
                LeadModel.class
        );

        long total = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                LeadModel.class
        );

        return new PageImpl<>(leads, pageable, total);
    }
}
