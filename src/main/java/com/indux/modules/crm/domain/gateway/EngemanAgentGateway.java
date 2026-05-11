package com.indux.modules.crm.domain.gateway;

import com.indux.modules.crm.domain.entity.EngemanAgent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EngemanAgentGateway {

    EngemanAgent save(EngemanAgent entity);

    Page<EngemanAgent> getAll(Pageable pageable);

    EngemanAgent getById(String id);

    EngemanAgent update(EngemanAgent entity, String id);

    void toggleStatus(String id);
}
