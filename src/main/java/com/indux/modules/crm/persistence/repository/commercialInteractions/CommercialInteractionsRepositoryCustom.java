package com.indux.modules.crm.persistence.repository.commercialInteractions;

import com.indux.modules.crm.application.dto.filter.CommercialInteractionsFilter;
import com.indux.modules.crm.persistence.model.CommercialInteractionsModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommercialInteractionsRepositoryCustom {

    Page<CommercialInteractionsModel> filter(CommercialInteractionsFilter filter, Pageable pageable);
}
