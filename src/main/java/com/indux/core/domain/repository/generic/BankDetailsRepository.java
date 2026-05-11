package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.employee.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetails, String> {
    Optional<BankDetails> findFirstByMatricula(String matricula);
}
