package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.FinanceComplaintReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceComplaintReasonRepository extends JpaRepository<FinanceComplaintReason, Long> {}

