package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.organization_chart.application.dtos.ProjectFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationProjectRepositoryCustom {
    Page<ProjectEntity> getWithFilter(ProjectFilter filter, Pageable pageable);

}
