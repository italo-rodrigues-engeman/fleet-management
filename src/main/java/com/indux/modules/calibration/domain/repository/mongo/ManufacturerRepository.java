package com.indux.modules.calibration.domain.repository.mongo;

import com.indux.modules.calibration.domain.entities.mongo.ManufacturerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturerRepository extends MongoRepository<ManufacturerEntity, String> {    
    Page<ManufacturerEntity> findAll(Pageable pageable);
    Page<ManufacturerEntity> findByStatus(Boolean status, Pageable pageable);
    
    @Query("{$or: [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    Page<ManufacturerEntity> findBySearchTerm(String searchTerm, Pageable pageable);
    
    @Query("{$and: [{'status': ?1}, {$or: [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}]}")
    Page<ManufacturerEntity> findBySearchTermAndStatus(String searchTerm, Boolean status, Pageable pageable);
}