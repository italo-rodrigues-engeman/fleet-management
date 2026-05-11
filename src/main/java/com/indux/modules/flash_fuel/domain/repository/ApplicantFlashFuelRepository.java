package com.indux.modules.flash_fuel.domain.repository;

import com.indux.modules.flash_fuel.domain.entities.ApplicantFlashFuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantFlashFuelRepository extends JpaRepository<ApplicantFlashFuel, Long> {

    ApplicantFlashFuel findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
}
