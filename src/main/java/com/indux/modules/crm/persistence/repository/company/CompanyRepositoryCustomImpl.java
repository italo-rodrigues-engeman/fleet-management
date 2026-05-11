package com.indux.modules.crm.persistence.repository.company;

import com.indux.modules.crm.application.dto.filter.CompanyFilter;
import com.indux.modules.crm.persistence.model.CompanyModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Slf4j
@Repository
public class CompanyRepositoryCustomImpl implements CompanyRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public CompanyRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private Criteria buildCriteria(CompanyFilter filter) {
        List<Criteria> criteria = new ArrayList<>();

        if (filter.cnpj() != null) {
            criteria.add(Criteria.where("cnpj").is(filter.cnpj()));
        }

        if (filter.name() != null) {
            criteria.add(Criteria.where("name").is(filter.name()));
        }

        if (filter.market() != null) {
            criteria.add(Criteria.where("market").is(filter.market()));
        }

        if (filter.sector() != null) {
            criteria.add(Criteria.where("sector").is(filter.sector()));
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
        } else if (filter.registrationDate() != null) {
            Date startOfDay = Date.from(filter.registrationDate().atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant());
            Date endOfDay = Date.from(filter.registrationDate().atTime(23, 59, 59, 999000000).atZone(java.time.ZoneId.of("America/Sao_Paulo")).toInstant());
            criteria.add(Criteria.where("registrationDate").gte(startOfDay).lte(endOfDay));
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<CompanyModel> filter(CompanyFilter filter, Pageable pageable) {

        Criteria criteria = buildCriteria(filter);

        Query query = new Query(criteria);
        query.with(pageable);

        List<CompanyModel> companies = mongoTemplate.find(
                query,
                CompanyModel.class
        );

        long total = mongoTemplate.count(
                Query.of(query).limit(-1).skip(-1),
                CompanyModel.class
        );

        return new PageImpl<>(companies, pageable, total);
    }

}
