package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.UnionDetailedResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.exception.UnionNotFoundException;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class GetUnionDetailedUseCase {
    
    private final UnionRepository unionRepository;
    private final LaborContractRepository laborContractRepository;
    private final LaborContractAddendumRepository laborContractAddendumRepository;
    private final UnionMapper unionMapper;
    private final LaborContractMapper laborContractMapper;
    
    public UnionDetailedResponseDTO execute(String unionId) {
        try {
            // Buscar sindicato
            Union union = unionRepository.findById(unionId)
                    .orElseThrow(() -> new UnionNotFoundException("Sindicato não encontrado com ID: " + unionId));
            
            // Buscar ACTs/CCTs do sindicato
            List<LaborContract> laborContracts = laborContractRepository.findBySindicatoTrabalhadoresId(unionId);
            
            // Converter para DTO detalhado com dados resumidos das ACTs/CCTs
            UnionDetailedResponseDTO result = unionMapper.toDetailedResponseDTOWithSummary(union, laborContracts);
            
            return result;
            
        } catch (UnionNotFoundException e) {
            log.warn("Sindicato não encontrado com ID: {}", unionId);
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar sindicato detalhado com ID: {}", unionId, e);
            throw new RuntimeException("Erro ao buscar sindicato detalhado: " + e.getMessage(), e);
        }
    }
}
