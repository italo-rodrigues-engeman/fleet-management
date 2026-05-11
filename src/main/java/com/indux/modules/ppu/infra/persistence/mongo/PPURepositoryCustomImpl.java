package com.indux.modules.ppu.infra.persistence.mongo;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
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
public class PPURepositoryCustomImpl implements PPURepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public PPURepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<PPUEntity> findByFilters(
            Long branch,
            Integer contractId,
            String platform,
            DocumentStatus status,
            String createdBy,
            Pageable pageable) {

        List<Criteria> criteria = new ArrayList<>();

        if (branch != null)
            criteria.add(Criteria.where("regionalId").is(branch));
        if (contractId != null)
            criteria.add(Criteria.where("contract.id").is(contractId));
        if (platform != null)
            criteria.add(Criteria.where("platforms").is(platform));
        if (status != null)
            criteria.add(Criteria.where("status").is(status));
        if (createdBy != null)
            criteria.add(Criteria.where("createdBy").is(createdBy));

        Query query = new Query();

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria));
        }

        long total = mongoTemplate.count(query, PPUEntity.class);
        List<PPUEntity> results = mongoTemplate.find(query.with(pageable), PPUEntity.class);

        return new PageImpl<>(results, pageable, total);
    }

}