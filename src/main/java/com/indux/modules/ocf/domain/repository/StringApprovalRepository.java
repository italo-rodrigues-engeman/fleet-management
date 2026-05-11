package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.StringApproval;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StringApprovalRepository extends MongoRepository<StringApproval, String> {
    
    List<StringApproval> findAll();
    
    List<StringApproval> findByAprovadoIsNotNull();
    
    List<StringApproval> findByRejeitadoIsNotNull();
}

