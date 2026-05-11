package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.entities.OccurrenceString;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OccurrenceStringRepository extends MongoRepository<OccurrenceString, String> {
    
    List<OccurrenceString> findByTypeAndActiveTrue(String type);
    
    List<OccurrenceString> findByActiveTrue();
    
    Optional<OccurrenceString> findByValueAndTypeAndActiveTrue(String value, String type);
    
    List<OccurrenceString> findByType(String type);
    
    List<OccurrenceString> findByCreatedByAndActiveTrue(UUID createdBy);
}
