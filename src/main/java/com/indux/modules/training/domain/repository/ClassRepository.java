package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.ClassEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface ClassRepository extends MongoRepository<ClassEntity, String>, ClassRepositoryCustom {
}
