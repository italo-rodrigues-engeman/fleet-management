package com.indux.modules.ppu.domain.repositories.mongo;

import com.indux.modules.ppu.application.projection.PendingRDOProjection;
import com.indux.modules.ppu.application.projection.RDOGridProjection;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RDORepository extends MongoRepository<RDOEntity, String>, RDORepositoryCustom {
    RDOEntity findBySequentialIdAndPlatform(Long code, String platform);

    Page<RDOEntity> findAllByPpuId(String ppuID, Pageable pageable);
    List<RDOEntity> findByPpuId(String ppuId);
    List<RDOEntity> findAllByPpuIdAndDateOrderByPlatformAsc(String ppuId, LocalDate date);

    Optional<RDOEntity> findFirstByPlatformAndStatusDPOrderBySequentialIdAsc(String platform, RDOStatusDP statusDP);

    boolean existsByPlatformAndPpuIdAndDate(String platform, String ppuId, LocalDate date);

    Optional<RDOEntity> findTopByPlatformAndPpuIdAndDateBeforeOrderByDateDesc(String platform, String ppuId, LocalDate beforeDate);

    @Query(fields = "{ id:1, data:1, sequentialId:1, branchName:1, platform:1, statusDP:1, statusOP:1, clientName:1, competence:1, contract:1 }")
    Page<RDOGridProjection> findAllByPlatformAndStatusOPAndDateBetween(String platform, RDOStatusOP opStatus, LocalDate start, LocalDate end, Pageable pageable);

    @Query(value = """
              { data: { $gte: :#{#start}, $lte: :#{#end} },
                projectId: :#{#project},
                $or: [
                  { statusDP: { $ne: "APPROVED" } },
                  { statusOP: { $ne: "APPROVED" } }
                ]
              }
            """)
    List<PendingRDOProjection> findPendingsInPeriodAndProjectId(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("project") Long project
    );

    //--- GRID ---//
    Integer countByStatusDP(RDOStatusDP statusDP);

    Integer countByStatusDPAndStatusOPNotIn(RDOStatusDP statusDP, List<RDOStatusOP> excludedStatusOP);

    Integer countByStatusOP(RDOStatusOP statusOP);

    @Query(value = "{ 'statusDP': ?0, $or: [ { 'competence': null }, { 'competence': ?1 } ] }")
    List<RDOEntity> findAllByStatusAndCompetenceNullOrDash(
            @Param("status") RDOStatusDP status,
            @Param("dash") String dash
    );

    @Query(value = "{ 'statusDP': ?0, 'projectId': ?2, 'competence': { $in: [ null, ?1 ] } }")
    List<RDOEntity> findAllByStatusAndCompetenceNullOrDashAndProject(
            @Param("status") RDOStatusDP status,
            @Param("dash") String dash,
            @Param("projectId") Long project
    );

    @Query("{ '_id': { $in: ?0 } }")
    List<RDOEntity> findByIdIn(List<String> ids);

    List<RDOEntity> findByPlatformInAndPpuIdAndDateBetween(Collection<String> platforms, String ppuId, LocalDate start, LocalDate end);
    List<RDOEntity> findAllByPlatformAndPpuIdAndDateBetween(
            String platform, String ppuId, LocalDate start, LocalDate end
    );

    List<RDOEntity> findAllByProjectIdAndDateBetween(Long projectId, LocalDate start, LocalDate end);
}
