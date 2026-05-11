package com.indux.modules.modulo_mega.persistence.repository;

import com.indux.modules.modulo_mega.persistence.model.PurchaseProcessDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseProcessRepository extends MongoRepository<PurchaseProcessDocument, String>, PurchaseProcessFilterRepository {
}

