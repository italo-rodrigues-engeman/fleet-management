package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationProjectRepository extends JpaRepository<ProjectEntity, Long>, OrganizationProjectRepositoryCustom {
    @Query("SELECT DISTINCT u FROM ProjectEntity u JOIN FETCH u.filial")
    Page<ProjectEntity> findAllBy(Pageable pageable);
    List<ProjectEntity> findByMegaCusInReduzido(Integer mega);
    List<ProjectEntity> findByHcmCcId(Integer hcm);
    List<ProjectEntity> findBySubordinateId(Long subordinate);
    List<ProjectEntity> findByContractId(Long subordinate);
    
    @Query(value = "SELECT id, mega, hcm, contrato, subordinado, ativo FROM tb_organograma_projeto WHERE contrato = :contractId", nativeQuery = true)
    List<Object[]> findProjectsDataByContractId(@Param("contractId") Long contractId);
    
    @Query(value = "SELECT f.filial_id, f.nome_filial FROM tb_filiais_hcm f INNER JOIN tb_organograma_projeto_filial pf ON pf.filial_id = f.filial_id WHERE pf.project_id = :projectId", nativeQuery = true)
    List<Object[]> findFiliaisByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT p FROM ProjectEntity p JOIN FETCH p.mega WHERE LOWER(p.mega.cusStDescricao) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY p.mega.cusStDescricao ASC")
    Page<ProjectEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM ProjectEntity p " +
           "LEFT JOIN FETCH p.mega " +
           "LEFT JOIN FETCH p.hcm " +
           "LEFT JOIN FETCH p.filial pf " +
           "LEFT JOIN FETCH pf.ccMega " +
           "LEFT JOIN FETCH p.subordinate " +
           "LEFT JOIN FETCH p.contract c " +
           "LEFT JOIN FETCH c.branch cb " +
           "LEFT JOIN FETCH p.branch pb " +
           "WHERE p.id = :id")
    Optional<ProjectEntity> findByIdWithDetails(@Param("id") Long id);

    List<ProjectEntity> findAllById(Iterable<Long> ids);

    List<ProjectEntity> findDistinctByFilial_FilialIdIn(List<Integer> filialIds);

    @Query("""
    SELECT DISTINCT p
    FROM ProjectEntity p
    JOIN p.filial f
    LEFT JOIN FETCH p.mega
    LEFT JOIN FETCH p.hcm
    LEFT JOIN FETCH p.subordinate
    LEFT JOIN FETCH p.contract c
    WHERE f.filialId IN :filialIds
    ORDER BY p.id
""")
    List<ProjectEntity> findHierarchyProjectsByFilialIds(@Param("filialIds") List<Integer> filialIds);

    @Query("""
    SELECT p
    FROM ProjectEntity p
    JOIN p.filial f
    LEFT JOIN FETCH p.mega
    LEFT JOIN FETCH p.hcm
    LEFT JOIN FETCH p.filial pf
    LEFT JOIN FETCH pf.ccMega
    LEFT JOIN FETCH p.subordinate
    LEFT JOIN FETCH p.contract c
    LEFT JOIN FETCH c.subordinate
    WHERE f.filialId = :filialId
    ORDER BY p.id
""")
    List<ProjectEntity> findHierarchyProjectsByFilialId(@Param("filialId") Integer filialId);

    @Query("""
    SELECT p
    FROM ProjectEntity p
    JOIN FETCH p.filial
    WHERE p.filialMegaId IN :filialMegaIds
""")
    List<ProjectEntity> findByFilialMegaIdIn(@Param("filialMegaIds") List<Long> filialMegaIds);

    @Query("""
    SELECT DISTINCT p
    FROM ProjectEntity p
    JOIN FETCH p.filial
    WHERE p.hcm.ccId IN :hcmCcIds
""")
    List<ProjectEntity> findByHcmCcIdInWithFilial(@Param("hcmCcIds") List<Integer> hcmCcIds);

}

