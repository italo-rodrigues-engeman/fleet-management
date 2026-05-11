package com.indux.modules.crm.persistence.repository.lead;

import com.indux.modules.crm.application.dto.filter.LeadFilter;
import com.indux.modules.crm.persistence.model.LeadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadRepositoryCustom {

    Page<LeadModel> filter(Pageable pageable, LeadFilter filter);
}
