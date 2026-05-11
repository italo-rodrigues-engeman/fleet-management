package com.indux.modules.ppu.domain.repositories.jpa;

import com.indux.modules.ppu.domain.entities.jpa.DatabaseSequencePPU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSequencePPURepository extends JpaRepository<DatabaseSequencePPU, Long> {
}
