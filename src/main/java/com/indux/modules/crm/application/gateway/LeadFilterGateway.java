package com.indux.modules.crm.application.gateway;

import com.indux.modules.crm.application.dto.filter.LeadFilter;
import com.indux.modules.crm.application.dto.response.LeadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadFilterGateway {

    Page<LeadResponse> filter(Pageable pageable, LeadFilter filter);
}
