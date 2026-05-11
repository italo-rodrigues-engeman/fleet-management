package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long>, OrganizationRepositoryCustom {
    @Query("SELECT m FROM OrganizationEntity m WHERE m.type = :type ORDER BY m.position ASC")
    Page<OrganizationEntity> findAllByType(Pageable pageable, @Param("type") OrganizationType type);

    @Query("SELECT m FROM OrganizationEntity m WHERE m.type = :type AND (LOWER(m.position) LIKE LOWER(CONCAT('%', :value, '%')) OR LOWER(m.collaborator.name) LIKE LOWER(CONCAT('%', :value, '%'))) ORDER BY m.position ASC")
    Page<OrganizationEntity> findAllByTypeSearch(Pageable pageable, @Param("type") OrganizationType type,
            @Param("value") Object value);

    boolean existsById(Long subordinate);

    boolean existsByCollaboratorId(UUID collaborator);

    Page<OrganizationDTO> findAllBy(Pageable pageable);

    List<OrganizationDTO> findBySubordinateId(Long subordinate);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.subordinate.id = :directorId AND o.active = true")
    List<OrganizationEntity> findAllSubordinatesByDirectorId(@Param("directorId") Long directorId);

    @Query("SELECT o.id FROM OrganizationEntity o WHERE o.subordinate.id = :directorId AND o.active = true")
    List<Long> findAllSubordinateIdsByDirectorId(@Param("directorId") Long directorId);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.collaborator.id IN :collaboratorIds AND o.active = true")
    List<OrganizationEntity> findAllByCollaboratorIdIn(@Param("collaboratorIds") List<UUID> collaboratorIds);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.collaborator.id = :collaboratorId AND o.active = true")
    Optional<OrganizationEntity> findByCollaboratorIdAndActiveTrue(@Param("collaboratorId") UUID collaboratorId);

}
