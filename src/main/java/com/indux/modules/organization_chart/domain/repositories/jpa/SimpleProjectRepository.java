package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.SimpleProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimpleProjectRepository extends JpaRepository<SimpleProjectEntity, Long> {

    @Query("SELECT s FROM SimpleProjectEntity s WHERE s.contractId IN :contractIds")
    List<SimpleProjectEntity> findByContractIdIn(@Param("contractIds") List<Long> contractIds);

    @Query("SELECT s FROM SimpleProjectEntity s WHERE s.subordinateId IN :subordinateIds AND s.contractId IS NULL")
    List<SimpleProjectEntity> findBySubordinateIdInAndContractIdIsNull(
            @Param("subordinateIds") List<Long> subordinateIds);

    @Query("SELECT s FROM SimpleProjectEntity s WHERE s.hcmId = :hcmId")
    List<SimpleProjectEntity> findByHcmId(@Param("hcmId") Integer hcmId);

    @Query(value = "SELECT sp.* FROM tb_organograma_projeto sp " +
            "INNER JOIN tb_organograma_projeto_filial pf ON pf.project_id = sp.id " +
            "WHERE pf.filial_id = :filialHcmId", nativeQuery = true)
    List<SimpleProjectEntity> findByFilialHcmId(@Param("filialHcmId") Long filialHcmId);

    @Query(value = "SELECT sp.* FROM tb_organograma_projeto sp " +
            "INNER JOIN tb_organograma_projeto_filial pf ON pf.project_id = sp.id " +
            "WHERE pf.filial_id IN :filialHcmIds", nativeQuery = true)
    List<SimpleProjectEntity> findByFilialHcmIdIn(@Param("filialHcmIds") List<Integer> filialHcmIds);
}
