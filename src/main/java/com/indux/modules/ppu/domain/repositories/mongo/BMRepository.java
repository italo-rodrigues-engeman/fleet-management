package com.indux.modules.ppu.domain.repositories.mongo;

import com.indux.modules.ppu.application.projection.BMProjection;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BMRepository extends MongoRepository<BMEntity, String> {
    Page<BMProjection> findAllBy(Pageable pageable);
}
