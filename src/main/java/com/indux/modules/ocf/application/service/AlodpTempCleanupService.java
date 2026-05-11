package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.domain.repositories.mongo.AlodpTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlodpTempCleanupService {
    
    private final AlodpTempRepository alodpTempRepository;
    
    /**
     * Remove logs da tabela alodp_temp baseado no CPF do colaborador
     * @param cpf CPF do colaborador
     * @return Número de registros removidos
     */
    public long cleanupLogsByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            log.warn("CPF não fornecido para limpeza de logs alodp_temp");
            return 0;
        }
        
        try {
            // Buscar logs existentes antes de deletar para logging
            var existingLogs = alodpTempRepository.findByCpf(cpf);
            log.info("Encontrados {} logs na tabela alodp_temp para o CPF: {}", existingLogs.size(), cpf);
            
            // Deletar logs por CPF
            long deletedCount = alodpTempRepository.deleteByCpf(cpf);
            
            log.info("Removidos {} logs da tabela alodp_temp para o CPF: {}", deletedCount, cpf);
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Erro ao limpar logs da tabela alodp_temp para o CPF: {}. Erro: {}", cpf, e.getMessage(), e);
            return 0;
        }
    }
}
