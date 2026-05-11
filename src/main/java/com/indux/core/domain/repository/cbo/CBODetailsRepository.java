package com.indux.core.domain.repository.cbo;

import com.indux.core.domain.model.cbo.CBODetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CBODetailsRepository extends MongoRepository<CBODetails,String> {
    Page<CBODetails> findAll(Pageable pageable);
    Optional<CBODetails> findById(String s);
}
