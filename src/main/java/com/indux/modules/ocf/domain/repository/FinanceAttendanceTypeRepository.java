package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.FinanceAttendanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceAttendanceTypeRepository extends JpaRepository<FinanceAttendanceType, Long> { }

