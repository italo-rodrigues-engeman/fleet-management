package com.indux.modules.ticket_santander.domain.repositories.jpa;

import com.indux.modules.ticket_santander.domain.entities.jpa.TicketSantanderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketSantanderRepository extends JpaRepository<TicketSantanderEntity, Long> {
    
    // Buscar tickets por funcionário
    List<TicketSantanderEntity> findByFuncionarioId(UUID funcionarioId);
    
    // Buscar tickets por CPF
    List<TicketSantanderEntity> findByCpf(String cpf);
    
    // Buscar tickets por status
    List<TicketSantanderEntity> findByStatus(String status);
    
    // Buscar tickets por tipo
    List<TicketSantanderEntity> findByTipoTicket(String tipoTicket);
    
    // Buscar ticket por número
    Optional<TicketSantanderEntity> findByNumeroTicket(String numeroTicket);
    
    // Buscar tickets por funcionário com paginação
    Page<TicketSantanderEntity> findByFuncionarioId(UUID funcionarioId, Pageable pageable);
    
    // Buscar tickets por CPF com paginação
    Page<TicketSantanderEntity> findByCpf(String cpf, Pageable pageable);
    
    // Buscar tickets por status com paginação
    Page<TicketSantanderEntity> findByStatus(String status, Pageable pageable);
    
    // Buscar tickets por período
    @Query("SELECT t FROM TicketSantanderEntity t WHERE t.dataCriacao BETWEEN :dataInicio AND :dataFim")
    List<TicketSantanderEntity> findByDataCriacaoBetween(@Param("dataInicio") java.time.LocalDateTime dataInicio, 
                                                         @Param("dataFim") java.time.LocalDateTime dataFim);
    
    // Contar tickets por status
    long countByStatus(String status);
    
    // Contar tickets por funcionário
    long countByFuncionarioId(UUID funcionarioId);
    
    // Buscar tickets recentes (últimos 30 dias)
    @Query("SELECT t FROM TicketSantanderEntity t WHERE t.dataCriacao >= :dataLimite ORDER BY t.dataCriacao DESC")
    List<TicketSantanderEntity> findTicketsRecentes(@Param("dataLimite") java.time.LocalDateTime dataLimite);
    
    // Buscar tickets por agência
    List<TicketSantanderEntity> findByNumeroAgencia(String numeroAgencia);
    
    // Buscar tickets por conta
    List<TicketSantanderEntity> findByNumeroConta(String numeroConta);
    
    // Buscar tickets por agência e conta
    List<TicketSantanderEntity> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);
    
    // Buscar tickets por CPF e agência
    List<TicketSantanderEntity> findByCpfAndNumeroAgencia(String cpf, String numeroAgencia);
    
    // Buscar tickets por CPF e conta
    List<TicketSantanderEntity> findByCpfAndNumeroConta(String cpf, String numeroConta);
}
