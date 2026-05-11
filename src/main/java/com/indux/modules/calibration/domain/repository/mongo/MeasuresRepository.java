package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasuresRepository extends MongoRepository<MeasuresEntity, String> {
    Page<MeasuresEntity> findAll(Pageable pageable);
    Page<MeasuresEntity> findByStatus(Boolean status, Pageable pageable);
    
    @Query("{$or: [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    Page<MeasuresEntity> findBySearchTerm(String searchTerm, Pageable pageable);
    
    @Query("{$and: [{'status': ?1}, {$or: [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}]}")
    Page<MeasuresEntity> findBySearchTermAndStatus(String searchTerm, Boolean status, Pageable pageable);
}