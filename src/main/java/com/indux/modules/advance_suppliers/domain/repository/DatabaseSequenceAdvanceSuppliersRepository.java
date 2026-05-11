package com.indux.modules.advance_suppliers.domain.repository;

import com.indux.modules.advance_suppliers.domain.entities.DatabaseSequenceAdvanceSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSequenceAdvanceSuppliersRepository extends JpaRepository<DatabaseSequenceAdvanceSupplier, String> {
}
