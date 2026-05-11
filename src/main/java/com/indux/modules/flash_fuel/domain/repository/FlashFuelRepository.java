package com.indux.modules.flash_fuel.domain.repository;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.flash_fuel.domain.entities.FlashFuel;
import com.indux.modules.flash_fuel.domain.entities.log.IdCodeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FlashFuelRepository extends MongoRepository<FlashFuel, String>, CustomFlashFuelRepository {
    Page<FlashFuel> findByApplicantId(UUID solicitanteId, Pageable pageable);

    Page<FlashFuel> findByRegionalIdIn(Set<Integer> filiais, Pageable pageable);

    Page<FlashFuel> findByRegionalId(Integer filial, Pageable pageable);

    Page<FlashFuel> findByCurrentStepInAndRegionalIdInAndStatusNotIn(Set<Integer> etapas, Set<Integer> regionais, List<DocumentStatus> rejectStatus, Pageable pageable);

    Page<FlashFuel> findByCurrentStepInAndStatusNotIn(Set<Integer> etapas, List<DocumentStatus> rejectStatus, Pageable pageable);

    List<IdCodeProjection> findByIdIn(List<String> ids);
}
