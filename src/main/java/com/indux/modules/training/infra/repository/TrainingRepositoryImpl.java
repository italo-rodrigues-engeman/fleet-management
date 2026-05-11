package com.indux.modules.training.infra.repository;

import com.indux.modules.training.domain.entity.InstituinEntity;
import com.indux.modules.training.domain.entity.TrainingEntity;
import com.indux.modules.training.domain.repository.CustomTrainingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TrainingRepositoryImpl implements CustomTrainingRepository {

    private final MongoTemplate mongoTemplate;

    public TrainingRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<TrainingEntity> findTrainingsWithFilters(List<String> filialHCMList, String search, String mandatory, String institutionId, Pageable pageable) {
        Criteria criteria = new Criteria();

        if ((filialHCMList != null && !filialHCMList.isEmpty()) || (mandatory != null && !mandatory.isEmpty())) {
            Criteria filialCriteria = new Criteria();
            
            if (filialHCMList != null && !filialHCMList.isEmpty() && mandatory != null && !mandatory.isEmpty()) {
                filialCriteria.andOperator(
                    Criteria.where("filialHCM").in(filialHCMList),
                    Criteria.where("mandatory").regex("^" + mandatory + "$")
                );
            } else if (filialHCMList != null && !filialHCMList.isEmpty()) {
                filialCriteria.and("filialHCM").in(filialHCMList);
            } else if (mandatory != null && !mandatory.isEmpty()) {
                filialCriteria.and("mandatory").regex("^" + mandatory + "$");
            }
            
            
            criteria.and("filiais").elemMatch(filialCriteria);
        }


        if (search != null && !search.isEmpty()) {
            criteria.and("name").regex(".*" + search + ".*");
        }

        // Filter by institution ID if provided
        if (institutionId != null && !institutionId.isEmpty()) {
            List<InstituinEntity> institutions = mongoTemplate.find(
                Query.query(Criteria.where("_id").is(institutionId)), 
                InstituinEntity.class
            );
            
            if (!institutions.isEmpty()) {
                List<String> trainingIds = institutions.stream()
                    .flatMap(inst -> inst.getTrainings().stream())
                    .map(TrainingEntity::getId)
                    .toList();
                
                if (!trainingIds.isEmpty()) {
                    criteria.and("_id").in(trainingIds);
                }
            }
        }

        Query query = new Query(criteria);
        long total = mongoTemplate.count(query, TrainingEntity.class);
        
        query.with(pageable);
        List<TrainingEntity> content = mongoTemplate.find(query, TrainingEntity.class);

        return new PageImpl<>(content, pageable, total);
    }
}