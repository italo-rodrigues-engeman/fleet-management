package com.indux.modules.crm.application.gateway;

import com.indux.modules.crm.application.dto.filter.CommercialInteractionsFilter;
import com.indux.modules.crm.application.dto.response.CommercialInteractionsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommercialInteractionsFilterGateway {

    Page<CommercialInteractionsResponse> filter(CommercialInteractionsFilter filter, Pageable pageable);
}
