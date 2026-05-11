package com.indux.modules.union_registration.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class DeleteLaborContractUseCase {
    
    private final LaborContractRepository laborContractRepository;
    private final LaborContractAddendumRepository addendumRepository;
    
    @Transactional
    public GenericMessage execute(String contractId) {
        LaborContract laborContract = laborContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrato trabalhista não encontrado com ID: " + contractId));
        
        if ("INATIVO".equals(laborContract.getStatusRegistro())) {
            throw new RuntimeException("Contrato já está inativo");
        }
        
        // Deletar todos os aditivos relacionados ao contrato (deleção em cascata)
        List<LaborContractAddendum> addendums = addendumRepository.findByContratoTrabalhistaId(contractId);
        if (!addendums.isEmpty()) {
            log.info("Deletando {} aditivo(s) relacionado(s) ao contrato {}", addendums.size(), contractId);
            addendumRepository.deleteAll(addendums);
        }
        
        // Deletar o contrato
        laborContractRepository.delete(laborContract);
        
        return new GenericMessage("Contrato trabalhista e seus aditivos excluídos com sucesso", 200);
    }
    
    @Transactional
    public GenericMessage softDelete(String contractId) {
        LaborContract laborContract = laborContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrato trabalhista não encontrado com ID: " + contractId));
        
        if ("INATIVO".equals(laborContract.getStatusRegistro())) {
            throw new RuntimeException("Contrato já está inativo");
        }
        
        // Desativar todos os aditivos relacionados ao contrato (soft delete em cascata)
        List<LaborContractAddendum> addendums = addendumRepository.findByContratoTrabalhistaId(contractId);
        if (!addendums.isEmpty()) {
            log.info("Desativando {} aditivo(s) relacionado(s) ao contrato {}", addendums.size(), contractId);
            addendums.forEach(addendum -> addendum.setStatusRegistro("INATIVO"));
            addendumRepository.saveAll(addendums);
        }
        
        // Desativar o contrato
        laborContract.setStatusRegistro("INATIVO");
        laborContractRepository.save(laborContract);
        
        return new GenericMessage("Contrato trabalhista e seus aditivos desativados com sucesso", 200);
    }
}
