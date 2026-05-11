package com.indux.modules.ticket_santander.domain.repositories.mongo;

import com.indux.modules.ticket_santander.domain.entities.mongo.TicketSantanderMongoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketSantanderMongoRepository extends MongoRepository<TicketSantanderMongoEntity, String> {
    
    // Buscar tickets por funcionário
    List<TicketSantanderMongoEntity> findByFuncionarioId(UUID funcionarioId);
    
    // Buscar tickets por CPF
    List<TicketSantanderMongoEntity> findByCpf(String cpf);
    
    // Buscar tickets por matrícula
    List<TicketSantanderMongoEntity> findByMatricula(String matricula);
    
    // Buscar tickets por status
    List<TicketSantanderMongoEntity> findByStatus(String status);
    
    // Buscar tickets por tipo
    List<TicketSantanderMongoEntity> findByTipoTicket(String tipoTicket);
    
    
    // Buscar tickets por funcionário com paginação
    Page<TicketSantanderMongoEntity> findByFuncionarioId(UUID funcionarioId, Pageable pageable);
    
    // Buscar tickets por CPF com paginação
    Page<TicketSantanderMongoEntity> findByCpf(String cpf, Pageable pageable);
    
    // Buscar tickets por status com paginação
    Page<TicketSantanderMongoEntity> findByStatus(String status, Pageable pageable);
    
    // Buscar tickets por período
    @Query("{'dataCriacao': {$gte: ?0, $lte: ?1}}")
    List<TicketSantanderMongoEntity> findByDataCriacaoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);
    
    // Contar tickets por status
    long countByStatus(String status);
    
    // Contar tickets por funcionário
    long countByFuncionarioId(UUID funcionarioId);
    
    // Buscar tickets recentes (últimos 30 dias)
    @Query("{'dataCriacao': {$gte: ?0}}")
    List<TicketSantanderMongoEntity> findTicketsRecentes(LocalDateTime dataLimite);
    
    // Buscar tickets por agência
    List<TicketSantanderMongoEntity> findByNumeroAgencia(String numeroAgencia);
    
    // Buscar tickets por conta
    List<TicketSantanderMongoEntity> findByNumeroConta(String numeroConta);
    
    // Buscar tickets por agência e conta
    List<TicketSantanderMongoEntity> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);
    
    // Buscar tickets por CPF e agência
    List<TicketSantanderMongoEntity> findByCpfAndNumeroAgencia(String cpf, String numeroAgencia);
    
    // Buscar tickets por CPF e conta
    List<TicketSantanderMongoEntity> findByCpfAndNumeroConta(String cpf, String numeroConta);
}
