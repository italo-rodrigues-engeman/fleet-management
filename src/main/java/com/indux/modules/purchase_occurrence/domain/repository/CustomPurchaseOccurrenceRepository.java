package com.indux.modules.purchase_occurrence.domain.repository;

import com.indux.modules.purchase_occurrence.domain.dto.PurchaseOccurrenceFilter;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPurchaseOccurrenceRepository {
    Page<PurchaseOccurrence> findByFilter(PurchaseOccurrenceFilter filter, Pageable pageable);

}
