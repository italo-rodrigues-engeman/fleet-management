package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.application.dto.OccurrenceFilter;
import com.indux.modules.ocf.domain.model.OcorrenciaFF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomOccurrenceRepository {
    Page<OcorrenciaFF> findByFilter(OccurrenceFilter filter, Pageable pageable);
    Page<OcorrenciaFF> findByFilterWithHierarchicalFilters(OccurrenceFilter filter, Pageable pageable, java.util.List<String> hcmIds);

}
