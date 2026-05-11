package com.indux.modules.flash_fuel.domain.repository;

import com.indux.modules.flash_fuel.domain.entities.DatabaseSequenceFlashFuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSequenceFlashFuelRepository extends JpaRepository<DatabaseSequenceFlashFuel, String> {
}
