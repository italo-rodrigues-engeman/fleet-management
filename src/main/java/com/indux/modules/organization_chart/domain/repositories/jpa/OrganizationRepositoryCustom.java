package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrganizationRepositoryCustom {
    Page<OrganizationEntity> findAllByTypeWithFilters(Pageable pageable, OrganizationType type, String searchTerm,
            List<Long> subordinadoIds);
}
