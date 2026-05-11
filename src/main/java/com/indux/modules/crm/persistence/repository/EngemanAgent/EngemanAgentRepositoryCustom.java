package com.indux.modules.crm.persistence.repository.EngemanAgent;

import com.indux.modules.crm.application.dto.filter.EngemanAgentFilter;
import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EngemanAgentRepositoryCustom {

    Page<EngemanAgentModel> filter(EngemanAgentFilter filter, Pageable pageable);
}
