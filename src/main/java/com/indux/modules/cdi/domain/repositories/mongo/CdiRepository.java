package com.indux.modules.cdi.domain.repositories.mongo;

import com.indux.modules.cdi.aplication.dtos.GetAllCdiDTO;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CdiRepository extends MongoRepository<CdiEntity, String>, CdiRepositoryCustom {
    Page<GetAllCdiDTO> findAllBy(Pageable pageable);
}
