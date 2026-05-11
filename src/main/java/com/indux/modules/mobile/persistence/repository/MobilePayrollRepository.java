package com.indux.modules.mobile.persistence.repository;

import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MobilePayrollRepository extends JpaRepository<PayrollEntity, String> {

    @Query("""
            SELECT DISTINCT p.competence
            FROM PayrollEntity p
            WHERE p.registration = :registration
            ORDER BY p.competence DESC
            """)
    Page<Date> findDistinctCompetencesByRegistration(@Param("registration") String registration, Pageable pageable);

    List<PayrollEntity> findByRegistrationAndCompetenceBetweenOrderByValueDesc(
            String registration,
            Date competenceStart,
            Date competenceEnd
    );
}
