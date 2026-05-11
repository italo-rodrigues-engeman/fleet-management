package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.HcmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationHcmRepositoryCustom {
    Page<HcmEntity> findHcmUsedByActiveProjects(String search, Pageable pageable);
}
