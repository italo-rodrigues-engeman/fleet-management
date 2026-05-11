package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.FinanceOccurrenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceOccurrenceTypeRepository extends JpaRepository<FinanceOccurrenceType, Long> {
}
