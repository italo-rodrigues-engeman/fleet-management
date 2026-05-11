package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.TrainingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingRepository extends MongoRepository<TrainingEntity, String>, CustomTrainingRepository {
}
