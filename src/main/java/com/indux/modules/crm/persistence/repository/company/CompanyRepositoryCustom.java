package com.indux.modules.crm.persistence.repository.company;

import com.indux.modules.crm.application.dto.filter.CompanyFilter;
import com.indux.modules.crm.persistence.model.CompanyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepositoryCustom {

    Page<CompanyModel> filter(CompanyFilter filter, Pageable pageable);
}
