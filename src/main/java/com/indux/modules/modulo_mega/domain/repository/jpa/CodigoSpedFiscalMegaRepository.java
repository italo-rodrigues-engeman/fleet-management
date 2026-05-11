package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoSpedFiscalMegaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoSpedFiscalMegaRepository extends JpaRepository<CodigoSpedFiscalMegaEntity, Long> {
}

