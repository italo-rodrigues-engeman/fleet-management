package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.MegaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMegaRepository
        extends JpaRepository<MegaEntity, Integer>, OrganizationMegaRepositoryCustom {
}
