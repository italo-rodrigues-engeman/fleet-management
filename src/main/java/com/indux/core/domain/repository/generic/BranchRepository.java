package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.employee.Filial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Filial, Long>, BranchRepositoryCustom {

}
