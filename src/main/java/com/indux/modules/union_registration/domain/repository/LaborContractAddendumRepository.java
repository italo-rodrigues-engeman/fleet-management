package com.indux.modules.union_registration.domain.repository;

import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface LaborContractAddendumRepository extends MongoRepository<LaborContractAddendum, String> {
    
    // Buscar aditivos por contrato trabalhista
    List<LaborContractAddendum> findByContratoTrabalhistaId(String contratoTrabalhistaId);
    
    // Buscar aditivos por contrato trabalhista com paginação
    Page<LaborContractAddendum> findByContratoTrabalhistaId(String contratoTrabalhistaId, Pageable pageable);
    
    // Buscar aditivos por contrato trabalhista e tipo
    List<LaborContractAddendum> findByContratoTrabalhistaIdAndTipo(String contratoTrabalhistaId, String tipo);
    
    // Buscar aditivos por contrato trabalhista, tipo e status
    Page<LaborContractAddendum> findByContratoTrabalhistaIdAndTipoAndStatusRegistro(
            String contratoTrabalhistaId, String tipo, String statusRegistro, Pageable pageable);
    
    // Buscar aditivos ativos por contrato trabalhista
    List<LaborContractAddendum> findByContratoTrabalhistaIdAndStatusRegistro(String contratoTrabalhistaId, String statusRegistro);
    
    // Verificar se existe aditivo com mesmo tipo e data para o mesmo contrato
    boolean existsByContratoTrabalhistaIdAndTipoAndDataInclusao(String contratoTrabalhistaId, TipoInstrumento tipo, LocalDate dataInclusao);
    
    // Buscar aditivos ordenados por sequência decrescente
    List<LaborContractAddendum> findByContratoTrabalhistaIdOrderBySequenciaDesc(String contratoTrabalhistaId);
    
    // Buscar o aditivo com maior sequência para um contrato
    LaborContractAddendum findFirstByContratoTrabalhistaIdOrderBySequenciaDesc(String contratoTrabalhistaId);
}
