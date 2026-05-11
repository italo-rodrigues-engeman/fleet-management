package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.domain.entity.SalaryCompositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryCompositionRepository extends JpaRepository<SalaryCompositionEntity, String>, SalaryCompositionRepositoryCustom {
}
