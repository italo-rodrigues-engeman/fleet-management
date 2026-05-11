package com.indux.modules.crm.application.gateway;

import com.indux.modules.crm.application.dto.filter.CompanyFilter;
import com.indux.modules.crm.application.dto.response.CompanySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyFilterGateway {
    Page<CompanySummary> filter(CompanyFilter filter, Pageable pageable);
}
