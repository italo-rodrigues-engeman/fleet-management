package com.indux.modules.crm.domain.gateway;

import com.indux.modules.crm.domain.entity.CommercialInteractions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommercialInteractionsGateway {

    CommercialInteractions create(CommercialInteractions commercialInteractions);

    Page<CommercialInteractions> getAll(Pageable pageable);

    Page<CommercialInteractions> getAllByCompany(Pageable pageable, String companyId);

    CommercialInteractions getById(String id);

    CommercialInteractions update(CommercialInteractions commercialInteractions, String id);

    void toggleStatus(String id);
}
