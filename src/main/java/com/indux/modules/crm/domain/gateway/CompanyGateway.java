package com.indux.modules.crm.domain.gateway;

import com.indux.modules.crm.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyGateway {
    Company save(Company company);

    Page<Company> getAll(Pageable pageable);

    Company getById(String id);

    Company update(Company company, String id);

    void toggleStatus(String id);

    void delete(String id);
}
