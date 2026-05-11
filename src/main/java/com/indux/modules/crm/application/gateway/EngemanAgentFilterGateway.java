package com.indux.modules.crm.application.gateway;

import com.indux.modules.crm.application.dto.filter.EngemanAgentFilter;
import com.indux.modules.crm.application.dto.response.EngemanAgentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EngemanAgentFilterGateway {

    Page<EngemanAgentResponse> filter(EngemanAgentFilter filter, Pageable pageable);
}
