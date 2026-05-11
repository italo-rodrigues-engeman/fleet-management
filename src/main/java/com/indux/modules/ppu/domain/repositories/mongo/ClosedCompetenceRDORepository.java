package com.indux.modules.ppu.domain.repositories.mongo;

import com.indux.modules.ppu.application.projection.CompetenceProjection;
import com.indux.modules.ppu.domain.entities.mongo.ClosedCompetenceRDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClosedCompetenceRDORepository extends MongoRepository<ClosedCompetenceRDO, String> {
    ClosedCompetenceRDO findByCompetence(String competence);

    Optional<ClosedCompetenceRDO> findByCompetenceAndProject(String competence, Long project);

    Boolean existsByCompetence(String competence);

    Boolean existsByCompetenceAndProject(String competence, Long project);

    Page<CompetenceProjection> findAllBy(Pageable pageable);

    @Query("{ 'year': ?0, 'project': ?1 }")
    List<ClosedCompetenceRDO> findAllByYearAndProject(Integer year, Long project);


}
