package com.indux.modules.ocf.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ocf.domain.entities.OccurrenceString;
import com.indux.modules.ocf.domain.repository.OccurrenceStringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OccurrenceStringService {
    
    private final OccurrenceStringRepository repository;
    
    public GenericMessage saveStrings(List<String> strings, String type, UUID userId) {
        // 1. Primeiro, desativar todas as strings existentes do tipo
        List<OccurrenceString> existingStrings = repository.findByTypeAndActiveTrue(type);
        existingStrings.forEach(string -> {
            string.setActive(false);
            string.setUpdatedAt(LocalDateTime.now());
        });
        if (!existingStrings.isEmpty()) {
            repository.saveAll(existingStrings);
        }
        
        // 2. Criar e salvar as novas strings
        List<OccurrenceString> stringsToSave = strings.stream()
                .map(String::trim)
                .filter(string -> !string.isEmpty())
                .map(string -> OccurrenceString.builder()
                        .id(UUID.randomUUID().toString())
                        .value(string)
                        .type(type)
                        .description(null)
                        .createdBy(userId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .active(true)
                        .build())
                .collect(Collectors.toList());
        
        int savedCount = 0;
        if (!stringsToSave.isEmpty()) {
            repository.saveAll(stringsToSave);
            savedCount = stringsToSave.size();
        }
        
        int removedCount = existingStrings.size();
        
        String message;
        if (savedCount == 0) {
            message = String.format("Removidas %d strings de ocorrências %s. Nenhuma nova string foi adicionada.", 
                    removedCount, type.toLowerCase());
        } else if (removedCount > 0) {
            message = String.format("Substituídas %d strings de ocorrências %s. Removidas %d antigas e adicionadas %d novas.", 
                    removedCount, type.toLowerCase(), removedCount, savedCount);
        } else {
            message = String.format("Adicionadas %d strings de ocorrências %s com sucesso.", 
                    savedCount, type.toLowerCase());
        }
        
        return new GenericMessage(message, 201);
    }
    
    public List<String> getStringsByType(String type) {
        return repository.findByTypeAndActiveTrue(type)
                .stream()
                .map(OccurrenceString::getValue)
                .collect(Collectors.toList());
    }
    
    public List<String> getAllStrings() {
        return repository.findByActiveTrue()
                .stream()
                .map(OccurrenceString::getValue)
                .collect(Collectors.toList());
    }
    
    public GenericMessage clearStringsByType(String type) {
        List<OccurrenceString> strings = repository.findByTypeAndActiveTrue(type);
        strings.forEach(string -> {
            string.setActive(false);
            string.setUpdatedAt(LocalDateTime.now());
        });
        repository.saveAll(strings);
        
        return new GenericMessage(
                String.format("Removidas %d strings de ocorrências %s.", 
                        strings.size(), 
                        type.toLowerCase()),
                200
        );
    }
}
