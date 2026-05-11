package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.HcmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrganizationHcmRepository extends JpaRepository<HcmEntity, Integer>, OrganizationHcmRepositoryCustom {
    @Query("SELECT m FROM HcmEntity m ORDER BY m.ccId ASC")
    Page<HcmEntity> findAllSortedById(Pageable pageable);

    Optional<Page<HcmEntity>> findByCcId(Integer ccId, Pageable pageable);

    Optional<Page<HcmEntity>> findByNomeCcContainingIgnoreCase(String nomeCc, Pageable pageable);
}
