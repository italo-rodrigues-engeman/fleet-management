package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.TicketAlodp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TicketAlodpRepository extends JpaRepository<TicketAlodp, Long> {
    
    Optional<TicketAlodp> findByProtocolo(Long protocolo);
    
    Optional<TicketAlodp> findByMatricula(String matricula);
    
    Optional<TicketAlodp> findByIdOcorrencia(Long idOcorrencia);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE tb_ticket_alodp SET status_chatwoot = :status WHERE id_ocorrencia = :idOcorrencia", nativeQuery = true)
    int updateStatusChatwootByIdOcorrencia(@Param("idOcorrencia") Long idOcorrencia, @Param("status") Integer status);
} 