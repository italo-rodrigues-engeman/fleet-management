package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.entities.SquadContractLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SquadContractLinkRepository extends JpaRepository<SquadContractLink, Long> {
    void deleteByTimeId(Long timeId);
}
