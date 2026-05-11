package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertiesRepository extends MongoRepository<PropertiesEntity, String> {
    Page<PropertiesEntity> findAll(Pageable pageable);
    Page<PropertiesEntity> findByStatus(Boolean status, Pageable pageable);
    
    @Query("{$or: [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    Page<PropertiesEntity> findBySearchTerm(String searchTerm, Pageable pageable);
    
    @Query("{$and: [{'status': ?1}, {$or: [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}]}")
    Page<PropertiesEntity> findBySearchTermAndStatus(String searchTerm, Boolean status, Pageable pageable);
}