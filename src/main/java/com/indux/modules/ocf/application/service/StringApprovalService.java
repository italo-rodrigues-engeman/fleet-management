package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.domain.model.StringApproval;
import com.indux.modules.ocf.domain.repository.StringApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StringApprovalService {
    
    @Autowired
    private StringApprovalRepository repository;
    
    /**
     * Salva uma lista de strings como aprovadas ou rejeitadas
     */
    public StringApproval saveStrings(List<String> strings, boolean aprovado, String userId) {
        // Limpar registros duplicados primeiro
        cleanupDuplicateRecords();
        
        // Buscar o registro existente
        List<StringApproval> allRecords = repository.findAll();
        StringApproval record;
        
        if (allRecords.isEmpty()) {
            // Se não existe nenhum registro, criar um novo
            record = new StringApproval();
        } else {
            // Se existe, usar o primeiro (deve ser o único)
            record = allRecords.get(0);
        }
        
        // Atualizar os campos apropriados
        if (aprovado) {
            record.setAprovado(strings);
        } else {
            record.setRejeitado(strings);
        }
        
        return repository.save(record);
    }
    
    /**
     * Remove registros duplicados, mantendo apenas um
     */
    private void cleanupDuplicateRecords() {
        List<StringApproval> allRecords = repository.findAll();
        if (allRecords.size() > 1) {
            // Manter apenas o primeiro registro
            StringApproval keepRecord = allRecords.get(0);
            repository.deleteAll();
            repository.save(keepRecord);
        }
    }
    
    /**
     * Lista todas as strings salvas
     */
    public List<StringApproval> getAllStringApprovals() {
        cleanupDuplicateRecords();
        return repository.findAll();
    }
    
    /**
     * Lista apenas strings aprovadas
     */
    public List<StringApproval> getApprovedStrings() {
        cleanupDuplicateRecords();
        List<StringApproval> allRecords = repository.findAll();
        if (!allRecords.isEmpty() && allRecords.get(0).getAprovado() != null) {
            return allRecords;
        }
        return List.of();
    }
    
    /**
     * Lista apenas strings rejeitadas
     */
    public List<StringApproval> getRejectedStrings() {
        cleanupDuplicateRecords();
        List<StringApproval> allRecords = repository.findAll();
        if (!allRecords.isEmpty() && allRecords.get(0).getRejeitado() != null) {
            return allRecords;
        }
        return List.of();
    }
    
    /**
     * Obtém o registro mais recente
     */
    public Optional<StringApproval> getLatestRecord() {
        cleanupDuplicateRecords();
        return repository.findAll().stream().findFirst();
    }
    
    /**
     * Limpa todas as strings (aprovadas e rejeitadas)
     */
    public void clearAllStrings() {
        repository.deleteAll();
    }
    
    /**
     * Limpa apenas strings aprovadas
     */
    public void clearApprovedStrings() {
        Optional<StringApproval> latest = getLatestRecord();
        if (latest.isPresent()) {
            StringApproval record = latest.get();
            record.setAprovado(null);
            repository.save(record);
        }
    }
    
    /**
     * Limpa apenas strings rejeitadas
     */
    public void clearRejectedStrings() {
        Optional<StringApproval> latest = getLatestRecord();
        if (latest.isPresent()) {
            StringApproval record = latest.get();
            record.setRejeitado(null);
            repository.save(record);
        }
    }
}

