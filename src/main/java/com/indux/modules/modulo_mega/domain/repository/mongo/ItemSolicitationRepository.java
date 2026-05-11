package com.indux.modules.modulo_mega.domain.repository.mongo;

import com.indux.modules.modulo_mega.domain.entities.mongo.ItemSolicitationEntity;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemSolicitationRepository extends MongoRepository<ItemSolicitationEntity, String>, ItemSolicitationRepositoryCustom {
    List<ItemSolicitationEntity> findByStatus(ItemSolicitationStatus status);
    long countByStatus(ItemSolicitationStatus status);
}

