package com.indux.modules.cdi.domain.repositories.mongo;

import com.indux.modules.cdi.domain.entities.mongo.ActionCDIEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActionCDIRepository extends MongoRepository<ActionCDIEntity, String> {
    Optional<ActionCDIEntity> findByCdiId(String cdiId);
    List<ActionCDIEntity> findAllByIsDevTrue();
}
