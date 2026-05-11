package com.indux.core.domain.repository.cbo;

import com.indux.core.domain.model.cbo.CourseTechnical;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTechnicalRepository extends MongoRepository<CourseTechnical,String> {
    Page<CourseTechnical> findAll(Pageable pageable);
    Page<CourseTechnical> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
