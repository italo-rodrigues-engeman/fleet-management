package com.indux.modules.purchase_occurrence.domain.repository;

import com.indux.modules.purchase_occurrence.domain.entities.DatabaseSequencePurchaseOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SequencePurchaseOccurrenceRepository extends JpaRepository<DatabaseSequencePurchaseOccurrence, Long> {
}
