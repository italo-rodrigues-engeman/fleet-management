package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class ListLaborContractsUseCase {
    
    private final LaborContractRepository laborContractRepository;
    private final LaborContractAddendumRepository addendumRepository;
    private final UnionRepository unionRepository;
    private final LaborContractMapper laborContractMapper;
    
    public Page<LaborContractResponseDTO> execute(String unionId, Pageable pageable) {
        // Validar se o sindicato existe
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Sindicato não encontrado com ID: " + unionId));
        
        // Buscar contratos do sindicato
        Page<LaborContract> contracts = laborContractRepository.findBySindicatoTrabalhadoresId(unionId, pageable);
        
        // Converter para DTO com informações do sindicato e última sequência de aditivo
        return contracts.map(contract -> {
            LaborContractResponseDTO dto = laborContractMapper.toResponseDTOWithUnionInfo(contract, union);
            dto.setUltimaSequenciaAditivo(buscarUltimaSequenciaAditivo(contract.getId()));
            return dto;
        });
    }
    
    public List<LaborContractResponseDTO> executeAll(String unionId) {
        // Validar se o sindicato existe
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Sindicato não encontrado com ID: " + unionId));
        
        // Buscar todos os contratos do sindicato
        List<LaborContract> contracts = laborContractRepository.findBySindicatoTrabalhadoresId(unionId);
        
        // Converter para DTO com informações do sindicato e última sequência de aditivo
        return contracts.stream()
                .map(contract -> {
                    LaborContractResponseDTO dto = laborContractMapper.toResponseDTOWithUnionInfo(contract, union);
                    dto.setUltimaSequenciaAditivo(buscarUltimaSequenciaAditivo(contract.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public Page<LaborContractResponseDTO> executeByType(String unionId, TipoInstrumento tipoInstrumento, Pageable pageable) {
        // Validar se o sindicato existe
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Sindicato não encontrado com ID: " + unionId));
        
        // Buscar contratos do sindicato por tipo
        Page<LaborContract> contracts = laborContractRepository.findBySindicatoTrabalhadoresIdAndTipoInstrumentoAndStatusRegistro(
                unionId, tipoInstrumento, "ATIVO", pageable);
        
        // Converter para DTO com informações do sindicato e última sequência de aditivo
        return contracts.map(contract -> {
            LaborContractResponseDTO dto = laborContractMapper.toResponseDTOWithUnionInfo(contract, union);
            dto.setUltimaSequenciaAditivo(buscarUltimaSequenciaAditivo(contract.getId()));
            return dto;
        });
    }
    
    /**
     * Busca a última sequência de aditivo para um contrato
     * Retorna null se não houver aditivos
     */
    private Integer buscarUltimaSequenciaAditivo(String contratoTrabalhistaId) {
        LaborContractAddendum ultimoAditivo = addendumRepository
                .findFirstByContratoTrabalhistaIdOrderBySequenciaDesc(contratoTrabalhistaId);
        
        if (ultimoAditivo == null || ultimoAditivo.getSequencia() == null) {
            return null;
        }
        
        return ultimoAditivo.getSequencia();
    }
}
