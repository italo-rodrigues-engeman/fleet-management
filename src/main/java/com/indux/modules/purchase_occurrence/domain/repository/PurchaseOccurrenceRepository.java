package com.indux.modules.purchase_occurrence.domain.repository;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrence;
import com.indux.modules.purchase_occurrence.domain.entities.log.IdCodeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PurchaseOccurrenceRepository extends MongoRepository<PurchaseOccurrence, String>, CustomPurchaseOccurrenceRepository {
    Page<PurchaseOccurrence> findByApplicantId(UUID solicitanteId, Pageable pageable);

    Page<PurchaseOccurrence> findByRegionalIdIn(Set<Integer> filiais, Pageable pageable);

    Page<PurchaseOccurrence> findByProjectIdIn(Set<Integer> contratos, Pageable pageable);

    Page<PurchaseOccurrence> findByRegionalIdInAndProjectIdIn(Set<Integer> filiais,
                                                     Set<Integer> contratos,
                                                     Pageable pageable);


    Page<PurchaseOccurrence> findByCurrentStepInAndStatusNotIn(Set<Integer> etapas, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<PurchaseOccurrence> findByCurrentStepInAndRegionalIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> filiais, List<DocumentStatus> rejectStatus, Pageable pageable);

    boolean existsByCodeID(String code);

    Page<PurchaseOccurrence> findByCurrentStepInAndProjectIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> contratos, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<PurchaseOccurrence> findByCurrentStepInAndRegionalIdInAndProjectIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> filiais, Set<Integer> contratos, List<DocumentStatus> rejectStatus, Pageable pageable);

    List<IdCodeProjection> findByIdIn(List<String> ids);
}
