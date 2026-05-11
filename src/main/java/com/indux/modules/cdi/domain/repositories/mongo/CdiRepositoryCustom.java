package com.indux.modules.cdi.domain.repositories.mongo;


import com.indux.modules.cdi.aplication.dtos.FilterCdiTranslateDTO;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CdiRepositoryCustom {
    Page<CdiEntity> getWithFilter(FilterCdiTranslateDTO filter, Pageable pageable);
}
