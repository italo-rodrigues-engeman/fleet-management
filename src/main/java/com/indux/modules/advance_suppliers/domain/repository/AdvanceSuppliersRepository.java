package com.indux.modules.advance_suppliers.domain.repository;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.advance_suppliers.domain.entities.AdvanceSuppliers;
import com.indux.modules.advance_suppliers.domain.entities.log.IdCodeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AdvanceSuppliersRepository extends MongoRepository<AdvanceSuppliers, String>, CustomAdvanceSuppliersRepository {
    Page<AdvanceSuppliers> findByApplicantId(UUID solicitanteId, Pageable pageable);

    Page<AdvanceSuppliers> findByRegionalIdIn(Set<Integer> regionais, Pageable pageable);

    Page<AdvanceSuppliers> findByProjectIdIn(Set<Integer> projetos, Pageable pageable);

    Page<AdvanceSuppliers> findByRegionalIdInAndProjectIdIn(Set<Integer> regionais,
                                                           Set<Integer> projetos,
                                                           Pageable pageable);


    Page<AdvanceSuppliers> findByCurrentStepInAndStatusNotIn(Set<Integer> etapas, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<AdvanceSuppliers> findByCurrentStepInAndRegionalIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> regionais, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<AdvanceSuppliers> findByCurrentStepInAndProjectIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> projetos, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<AdvanceSuppliers> findByCurrentStepInAndRegionalIdInAndProjectIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> regionais, Set<Integer> projetos, List<DocumentStatus> rejectStatus, Pageable pageable);

    List<IdCodeProjection> findByIdIn(List<String> ids);
}
