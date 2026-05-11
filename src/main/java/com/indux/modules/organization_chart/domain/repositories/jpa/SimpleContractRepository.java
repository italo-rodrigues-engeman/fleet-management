package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.SimpleContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SimpleContractRepository extends JpaRepository<SimpleContractEntity, Long> {
    
    @Query("SELECT s FROM SimpleContractEntity s WHERE s.subordinateId IN :subordinateIds")
    List<SimpleContractEntity> findBySubordinateIdIn(@Param("subordinateIds") List<Long> subordinateIds);
    
    @Query(value = "SELECT id, nome, os FROM tb_organograma_contrato WHERE id = :id", nativeQuery = true)
    Map<String, Object> findContractBasicDataById(@Param("id") Long id);
}
