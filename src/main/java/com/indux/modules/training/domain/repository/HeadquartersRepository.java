package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.HeadquartersEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadquartersRepository extends MongoRepository<HeadquartersEntity, String> {
}
