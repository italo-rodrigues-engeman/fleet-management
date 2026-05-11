package com.indux.modules.union_registration.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.domain.exception.UnionNotFoundException;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
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
public class DeleteUnionUseCase {
    
    private final UnionRepository unionRepository;
    private final LaborContractRepository laborContractRepository;
    private final LaborContractAddendumRepository addendumRepository;
    
    @Transactional
    public GenericMessage execute(String id) {
        Union union = unionRepository.findById(id)
                .orElseThrow(() -> new UnionNotFoundException("Sindicato não encontrado com ID: " + id));
        
        if ("INATIVO".equals(union.getStatusRegistro())) {
            throw new RuntimeException("Sindicato já está inativo");
        }
        
        // Buscar todos os contratos relacionados ao sindicato
        List<LaborContract> contracts = laborContractRepository.findBySindicatoTrabalhadoresId(id);
        
        int totalAddendums = 0;
        
        // Deletar todos os aditivos de cada contrato
        for (LaborContract contract : contracts) {
            List<LaborContractAddendum> addendums = addendumRepository.findByContratoTrabalhistaId(contract.getId());
            if (!addendums.isEmpty()) {
                totalAddendums += addendums.size();
                addendumRepository.deleteAll(addendums);
            }
        }
        
        // Deletar todos os contratos
        if (!contracts.isEmpty()) {
            log.info("Deletando {} contrato(s) e {} aditivo(s) relacionado(s) ao sindicato {}", 
                    contracts.size(), totalAddendums, id);
            laborContractRepository.deleteAll(contracts);
        }
        
        // Deletar o sindicato
        unionRepository.delete(union);
        
        return new GenericMessage("Sindicato, seus contratos e aditivos excluídos com sucesso", 200);
    }
    
    @Transactional
    public GenericMessage softDelete(String id) {
        Union union = unionRepository.findById(id)
                .orElseThrow(() -> new UnionNotFoundException("Sindicato não encontrado com ID: " + id));
        
        if ("INATIVO".equals(union.getStatusRegistro())) {
            throw new RuntimeException("Sindicato já está inativo");
        }
        
        // Buscar todos os contratos relacionados ao sindicato
        List<LaborContract> contracts = laborContractRepository.findBySindicatoTrabalhadoresId(id);
        
        int totalAddendums = 0;
        
        // Desativar todos os aditivos de cada contrato
        for (LaborContract contract : contracts) {
            List<LaborContractAddendum> addendums = addendumRepository.findByContratoTrabalhistaId(contract.getId());
            if (!addendums.isEmpty()) {
                totalAddendums += addendums.size();
                addendums.forEach(addendum -> addendum.setStatusRegistro("INATIVO"));
                addendumRepository.saveAll(addendums);
            }
        }
        
        // Desativar todos os contratos
        if (!contracts.isEmpty()) {
            log.info("Desativando {} contrato(s) e {} aditivo(s) relacionado(s) ao sindicato {}", 
                    contracts.size(), totalAddendums, id);
            contracts.forEach(contract -> contract.setStatusRegistro("INATIVO"));
            laborContractRepository.saveAll(contracts);
        }
        
        // Desativar o sindicato
        union.setStatusRegistro("INATIVO");
        unionRepository.save(union);
        
        return new GenericMessage("Sindicato, seus contratos e aditivos desativados com sucesso", 200);
    }
}
