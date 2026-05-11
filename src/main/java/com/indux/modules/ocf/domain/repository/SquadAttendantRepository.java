package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.entities.SquadAttendant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SquadAttendantRepository extends JpaRepository<SquadAttendant, Long> {
    void deleteBySquadId(Long timeId);
}
