package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.CCMegaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationCCMegaRepository extends JpaRepository<CCMegaEntity, Integer> {
    @Query("SELECT m FROM CCMegaEntity m ORDER BY CAST(REPLACE(m.cusStApelido, '.', '') AS integer) ASC")
    Page<CCMegaEntity> findAllSortedByApelido(Pageable pageable);

    @Query("SELECT m FROM CCMegaEntity m WHERE LOWER(m.cusStApelido) LIKE LOWER(CONCAT('%', :apelido, '%')) OR LOWER(m.cusStDescricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    Page<CCMegaEntity> searchByApelidoOrDescricao(@Param("apelido") String apelido, @Param("descricao") String descricao, Pageable pageable);
}
