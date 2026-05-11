package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationFilialRepository extends JpaRepository<FilialHcmEntity, Integer>, OrganizationFilialRepositoryCustom {
    @Query("SELECT m FROM FilialHcmEntity m ORDER BY m.filialId ASC")
    Page<FilialHcmEntity> findAllSortedById(Pageable pageable);

    Optional<Page<FilialHcmEntity>> findByFilialId(Integer filialId, Pageable pageable);
    Optional<Page<FilialHcmEntity>> findByNomeFilialContainingIgnoreCase(String nomeFilial, Pageable pageable);

    @Query("SELECT m FROM FilialHcmEntity m WHERE m.status IS NULL ORDER BY m.filialId ASC")
    Page<FilialHcmEntity> findAllWithStatusNullSortedById(Pageable pageable);


}