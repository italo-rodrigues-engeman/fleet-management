package com.indux.modules.modulo_mega.domain.repository;

import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntitySolicitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MegaEntitySolicitationRepository extends JpaRepository<MegaEntitySolicitation, String> {
    // Busca otimizada: traz apenas solicitações do item específico
    List<MegaEntitySolicitation> findByIdItem(Integer idItem);
}