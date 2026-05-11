package com.indux.core.domain.repository.cbo;

import com.indux.core.domain.model.cbo.CourseSuperior;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseSuperiorRepository extends MongoRepository<CourseSuperior,String> {
    Page<CourseSuperior> findAll(Pageable pageable);
    Page<CourseSuperior> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
