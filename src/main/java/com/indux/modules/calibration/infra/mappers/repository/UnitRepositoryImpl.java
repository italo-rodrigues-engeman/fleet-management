package com.indux.modules.calibration.infra.mappers.repository;

import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import com.indux.modules.calibration.domain.entities.mongo.UnitMeasuresEntity;
import com.indux.modules.calibration.domain.repository.mongo.UnitRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UnitRepositoryImpl implements UnitRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public UnitRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<UnitMeasuresEntity> findByFilters(Boolean status, String measureId, String searchTerm, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (StringUtils.hasText(measureId)) {
            criteriaList.add(Criteria.where("measures.$id").is(new ObjectId(measureId)));
        }

        if (StringUtils.hasText(searchTerm)) {
            Query measureQuery = Query.query(
                Criteria.where("name").regex(searchTerm, "i")
            );
            List<MeasuresEntity> measures = 
                mongoTemplate.find(measureQuery, MeasuresEntity.class);
            
            List<String> measureIds = measures.stream()
                .map(MeasuresEntity::getId)
                .filter(StringUtils::hasText)
                .collect(ArrayList::new, (list, item) -> {
                    if (isValidObjectId(item)) {
                        list.add(item);
                    }
                }, ArrayList::addAll);
            
            List<Criteria> searchCriterias = new ArrayList<>();
            searchCriterias.add(Criteria.where("name").regex(searchTerm, "i"));
            searchCriterias.add(Criteria.where("abbreviation").regex(searchTerm, "i"));
            
            if (!measureIds.isEmpty()) {
                searchCriterias.add(Criteria.where("measures.$id").in(measureIds));
            }
            
            Criteria searchCriteria = new Criteria().orOperator(searchCriterias);
            criteriaList.add(searchCriteria);
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<UnitMeasuresEntity> entities = mongoTemplate.find(query, UnitMeasuresEntity.class);
        long total = mongoTemplate.count(Query.of(query).skip(-1).limit(-1), UnitMeasuresEntity.class);

        return new PageImpl<>(entities, pageable, total);
    }

    private boolean isValidObjectId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        try {
            new org.bson.types.ObjectId(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}