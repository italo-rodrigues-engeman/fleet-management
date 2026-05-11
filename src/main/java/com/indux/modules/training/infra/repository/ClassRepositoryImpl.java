package com.indux.modules.training.infra.repository;

import com.indux.modules.training.domain.entity.ClassEntity;
import com.indux.modules.training.domain.repository.ClassRepositoryCustom;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClassRepositoryImpl implements ClassRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public ClassRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ClassEntity> findClassesFilter(List<String> trainingIds, String registration) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        
        if (trainingIds != null && !trainingIds.isEmpty()) {
            criteria.and("training").in(trainingIds);
        }
        
        if (registration != null) {
            criteria.and("collaborators").elemMatch(Criteria.where("registration").is(registration));
        }
        
        query.addCriteria(criteria);
        return mongoTemplate.find(query, ClassEntity.class);
    }
}