package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoSituacaoTributariaMegaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoSituacaoTributariaMegaRepository extends JpaRepository<CodigoSituacaoTributariaMegaEntity, Long> {
}

