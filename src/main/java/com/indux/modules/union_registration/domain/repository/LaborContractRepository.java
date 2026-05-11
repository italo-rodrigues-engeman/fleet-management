package com.indux.modules.union_registration.domain.repository;

import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborContractRepository extends MongoRepository<LaborContract, String> {
    
    // Buscar por sindicato
    List<LaborContract> findBySindicatoTrabalhadoresId(String sindicatoId);
    
    // Buscar por sindicato com paginação
    Page<LaborContract> findBySindicatoTrabalhadoresId(String sindicatoId, Pageable pageable);
    
    // Buscar por tipo de instrumento
    List<LaborContract> findByTipoInstrumento(TipoInstrumento tipoInstrumento);
    
    // Buscar por status
    List<LaborContract> findByStatusRegistro(String statusRegistro);
    
    // Buscar contratos ativos por sindicato
    List<LaborContract> findBySindicatoTrabalhadoresIdAndStatusRegistro(String sindicatoId, String status);
    
    // Buscar por UF principal
    List<LaborContract> findByUfPrincipal(String uf);
    
    // Buscar por categoria CBO
    List<LaborContract> findByCategoriaPrincipalCBO(String categoria);
    
    // Buscar com filtros combinados (para listagem paginada)
    Page<LaborContract> findBySindicatoTrabalhadoresIdAndTipoInstrumentoAndStatusRegistro(
            String sindicatoId, TipoInstrumento tipoInstrumento, String status, Pageable pageable);
    
    // Buscar contratos vigentes (data atual entre início e fim)
    List<LaborContract> findByDataInicioVigenciaLessThanEqualAndDataFimVigenciaGreaterThanEqual(
            java.time.LocalDate dataInicio, java.time.LocalDate dataFim);
}
