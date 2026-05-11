package com.indux.modules.purchase_occurrence.domain.repository;

import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrenceCauses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOccurrenceCausesRepository extends JpaRepository<PurchaseOccurrenceCauses, Long> {
}
