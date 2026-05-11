package com.indux.modules.employee_history.domain.repository;

import com.indux.modules.employee_history.domain.entity.DaysWorked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DaysWorkedRepository extends JpaRepository<DaysWorked, String>, DaysWorkedRepositoryCustom {
}
