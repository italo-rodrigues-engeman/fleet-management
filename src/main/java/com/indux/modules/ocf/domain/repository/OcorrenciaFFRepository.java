package com.indux.modules.ocf.domain.repository;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ocf.domain.model.OcorrenciaFF;
import com.indux.modules.ocf.domain.model.log.IdCodeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface OcorrenciaFFRepository extends MongoRepository<OcorrenciaFF, String>, CustomOccurrenceRepository {
    Page<OcorrenciaFF> findByRegionalIdIn(Set<Integer> filiais, Pageable pageable);

    Page<OcorrenciaFF> findByProjectIdIn(Set<Integer> contratos, Pageable pageable);

    Page<OcorrenciaFF> findByRegionalIdInAndProjectIdIn(Set<Integer> filiais,
                                                     Set<Integer> contratos,
                                                     Pageable pageable);

    //    Page<OcorrenciaFF> findBySolicitanteID(UUID solicitanteID);
    Page<OcorrenciaFF> findBySolicitanteId(UUID solicitanteId, Pageable pageable);

    Page<OcorrenciaFF> findByCurrentStepInAndStatusNotIn(Set<Integer> etapas, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<OcorrenciaFF> findByCurrentStepInAndRegionalIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> filiais, List<DocumentStatus> rejectStatus, Pageable pageable);

    boolean existsByCodeID(String code);

    Page<OcorrenciaFF> findByCurrentStepInAndProjectIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> contratos, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<OcorrenciaFF> findByCurrentStepInAndRegionalIdInAndProjectIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> filiais, Set<Integer> contratos, List<DocumentStatus> rejectStatus, Pageable pageable);

    List<IdCodeProjection> findByIdIn(List<String> ids);

    Optional<OcorrenciaFF> findByNumeroDoProtocolo(String numeroDoProtocolo);

    boolean existsByNumeroDoProtocolo(String numeroDoProtocolo);

    // Método específico para exportação - busca todas as ocorrências sem paginação
    List<OcorrenciaFF> findAllForExport();

}
