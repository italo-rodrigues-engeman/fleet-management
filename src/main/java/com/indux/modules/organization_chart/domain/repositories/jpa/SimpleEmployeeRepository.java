package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.SimpleEmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimpleEmployeeRepository extends JpaRepository<SimpleEmployeeEntity, UUID> {
    
    @Query("SELECT s FROM SimpleEmployeeEntity s WHERE s.costCenterId IN :costCenterIds")
    List<SimpleEmployeeEntity> findByCostCenterIdIn(@Param("costCenterIds") List<String> costCenterIds);
}
