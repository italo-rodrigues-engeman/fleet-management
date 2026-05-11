package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.FinanceOccurrenceComplaintType;
import com.indux.modules.ocf.domain.model.FinanceOccurrenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceOccurrenceComplaintTypeRepository extends JpaRepository<FinanceOccurrenceComplaintType, Long> {
    long countByOccurrencesContains(FinanceOccurrenceType child);

    List<FinanceOccurrenceComplaintType> findByOccurrencesContains(FinanceOccurrenceType child);
    
    @Query("SELECT DISTINCT f FROM FinanceOccurrenceComplaintType f LEFT JOIN FETCH f.occurrences ORDER BY f.id")
    List<FinanceOccurrenceComplaintType> findAllWithOccurrences();
}
