package com.indux.modules.training.infra.repository;

import com.indux.modules.training.domain.entity.InstituinEntity;
import com.indux.modules.training.domain.repository.CustomInstituinRepository;
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
public class InstituinRepositoryImpl implements CustomInstituinRepository {
    
    private final MongoTemplate mongoTemplate;
    
    public InstituinRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public Page<InstituinEntity> findBySearchAndTrainingIds(String search, List<String> trainingIds, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();
        
        if (search != null && !search.trim().isEmpty()) {
            criteriaList.add(Criteria.where("name").regex(search.trim(), "i"));
        }
        
        if (trainingIds != null && !trainingIds.isEmpty()) {
            criteriaList.add(Criteria.where("trainings").in(trainingIds));
        }
        
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        List<InstituinEntity> entities = mongoTemplate.find(query, InstituinEntity.class);
        long total = mongoTemplate.count(Query.of(query).skip(-1).limit(-1), InstituinEntity.class);
        
        return new PageImpl<>(entities, pageable, total);
    }
}