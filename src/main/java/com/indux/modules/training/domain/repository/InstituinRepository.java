package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.InstituinEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstituinRepository extends MongoRepository<InstituinEntity, String>, CustomInstituinRepository {
    Page<InstituinEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
