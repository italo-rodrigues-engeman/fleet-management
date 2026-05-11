package com.indux.modules.calibration.infra.mappers.repository;

import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.ManufacturerEntity;
import com.indux.modules.calibration.domain.repository.mongo.EquipamentRepositoryCustom;
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
public class EquipamentRepositoryImpl implements EquipamentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public EquipamentRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<EquipamentEntity> findByFilters(Boolean status, String manufacturerId, String propertiesId, String searchTerm, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (StringUtils.hasText(manufacturerId) && isValidObjectId(manufacturerId)) {
            criteriaList.add(Criteria.where("manufacturer.$id").is(new org.bson.types.ObjectId(manufacturerId)));
        }

        if (StringUtils.hasText(propertiesId) && isValidObjectId(propertiesId)) {
            criteriaList.add(Criteria.where("properties").elemMatch(Criteria.where("$id").is(new org.bson.types.ObjectId(propertiesId))));
        }

        if (StringUtils.hasText(searchTerm)) {
            Query manufacturerQuery = Query.query(
                Criteria.where("name").regex(searchTerm, "i")
            );
            List<ManufacturerEntity> manufacturers = 
                mongoTemplate.find(manufacturerQuery,ManufacturerEntity.class);
            
            List<String> manufacturerIds = manufacturers.stream()
                .map(ManufacturerEntity::getId)
                .filter(StringUtils::hasText)
                .collect(ArrayList::new, (list, item) -> {
                    if (isValidObjectId(item)) {
                        list.add(item);
                    }
                }, ArrayList::addAll);
            
            List<Criteria> searchCriterias = new ArrayList<>();
            searchCriterias.add(Criteria.where("name").regex(searchTerm, "i"));
            searchCriterias.add(Criteria.where("description").regex(searchTerm, "i"));
            
            if (!manufacturerIds.isEmpty()) {
                searchCriterias.add(Criteria.where("manufacturer").in(manufacturerIds));
            }
            
            Criteria searchCriteria = new Criteria().orOperator(searchCriterias);
            criteriaList.add(searchCriteria);
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<EquipamentEntity> entities = mongoTemplate.find(query, EquipamentEntity.class);
        long total = mongoTemplate.count(Query.of(query).skip(-1).limit(-1), EquipamentEntity.class);

        return new PageImpl<>(entities, pageable, total);
    }

    /**
     * Validates if a string is a valid MongoDB ObjectId hexadecimal representation
     */
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