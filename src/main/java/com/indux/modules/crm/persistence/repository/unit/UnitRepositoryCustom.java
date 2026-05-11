package com.indux.modules.crm.persistence.repository.unit;

import com.indux.modules.crm.application.dto.filter.UnitFilter;
import com.indux.modules.crm.application.dto.filter.UnitFilterAll;
import com.indux.modules.crm.persistence.model.UnitModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UnitRepositoryCustom {

    Page<UnitModel> filter(UnitFilter filter, String companyId, Pageable pageable);

    Page<UnitModel> filterAll(UnitFilterAll filter, Pageable pageable);
}
