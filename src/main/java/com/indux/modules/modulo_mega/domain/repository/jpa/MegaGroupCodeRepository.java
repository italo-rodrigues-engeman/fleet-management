package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.MegaGroupCode;
import com.indux.modules.modulo_mega.domain.repository.MegaGroupCodeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MegaGroupCodeRepository extends JpaRepository<MegaGroupCode,Integer>, JpaSpecificationExecutor<MegaGroupCode>, MegaGroupCodeRepositoryCustom {
}
