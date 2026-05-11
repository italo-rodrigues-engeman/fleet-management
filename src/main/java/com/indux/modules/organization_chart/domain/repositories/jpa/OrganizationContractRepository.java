package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationContractRepository extends JpaRepository<ContractEntity,Long>, OrganizationContractRepositoryCustom {
    @Query("SELECT m FROM ContractEntity m ORDER BY m.name ASC")
    Page<ContractEntity> findAllBy(Pageable pageable);
    
    @Query("SELECT m FROM ContractEntity m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY m.name ASC")
    Page<ContractEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT new com.indux.modules.organization_chart.application.dtos.ContractDTO(c.id, c.name, c.nickname, c.os, c.type, c.subordinate, c.hierarchy, null) FROM ContractEntity c WHERE c.subordinate.id = :subordinate")
    List<ContractDTO> findBySubordinateId(@Param("subordinate") Long subordinate);
    
    @Query("SELECT c FROM ContractEntity c WHERE c.subordinate.id IN :subordinateIds")
    List<ContractEntity> findBySubordinateIdIn(@Param("subordinateIds") List<Long> subordinateIds);
    
    @Query("SELECT c FROM ContractEntity c LEFT JOIN FETCH c.subordinate WHERE c.id = :id")
    Optional<ContractEntity> findByIdWithoutBranch(@Param("id") Long id);

}