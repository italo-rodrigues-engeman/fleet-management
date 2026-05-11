package com.indux.modules.ppu.infra.persistence.mongo;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PPURepositoryCustom {
    Page<PPUEntity> findByFilters(
            Long branch,
            Integer contractId,
            String platform,
            DocumentStatus status,
            String createdBy,
            Pageable pageable);
}