package com.indux.modules.union_registration.domain.repository;

import com.indux.modules.union_registration.domain.model.Union;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnionRepository extends MongoRepository<Union, String>, UnionRepositoryCustom {
    
    boolean existsByCnpj(String cnpj);
    
    Optional<Union> findByCnpj(String cnpj);
}
