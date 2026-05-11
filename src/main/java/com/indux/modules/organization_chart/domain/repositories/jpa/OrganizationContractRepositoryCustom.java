package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrganizationContractRepositoryCustom {
    Page<ContractEntity> findAllWithFilters(String searchTerm, List<Long> organizationIds, ContractType tipo,
            Pageable pageable);
}
