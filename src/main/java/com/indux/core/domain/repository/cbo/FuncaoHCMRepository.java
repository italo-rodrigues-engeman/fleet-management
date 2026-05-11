package com.indux.core.domain.repository.cbo;

import com.indux.core.domain.model.cbo.FuncaoHCM;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncaoHCMRepository extends MongoRepository<FuncaoHCM, String>, FuncaoRepositoryCustom {
    Optional<FuncaoHCM> findByHcmId(String s);
}
