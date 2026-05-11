package com.indux.modules.crm.domain.gateway;

import com.indux.modules.crm.domain.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadGateway {

    Lead save(Lead entity, String unitId);

    Lead getById(String id);

    Page<Lead> getAllByCompany(Pageable pageable, String companyId);

    Page<Lead> getAll(Pageable pageable);

    Lead update(Lead entity, String id);

    void toggleStatus(String id);

    void delete(String id);
}
