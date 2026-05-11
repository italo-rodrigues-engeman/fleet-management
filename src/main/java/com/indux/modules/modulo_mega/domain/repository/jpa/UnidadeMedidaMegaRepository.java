package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.UnidadeMedidaMegaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadeMedidaMegaRepository extends JpaRepository<UnidadeMedidaMegaEntity, String> {
}
