package com.indux.modules.ocf.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ocf.application.dto.OccurrenceStringRequest;
import com.indux.modules.ocf.application.service.OccurrenceStringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ocf/occurrence-strings")
@RequiredArgsConstructor
public class OccurrenceStringController {
    
    private final OccurrenceStringService service;
    
    /**
     * Salva uma lista de strings para ocorrências pertinentes
     */
    @PostMapping("/pertinentes")
    public ResponseEntity<GenericMessage> savePertinentes(
            @RequestBody @Valid OccurrenceStringRequest request,
            JwtAuthenticationToken jwt) {
        
        GenericMessage response = service.saveStrings(request.strings(), "PERTINENTE", UUID.fromString(jwt.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Salva uma lista de strings para ocorrências não pertinentes
     */
    @PostMapping("/nao-pertinentes")
    public ResponseEntity<GenericMessage> saveNaoPertinentes(
            @RequestBody @Valid OccurrenceStringRequest request,
            JwtAuthenticationToken jwt) {
        
        GenericMessage response = service.saveStrings(request.strings(), "NAO_PERTINENTE", UUID.fromString(jwt.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lista todas as strings de ocorrências pertinentes
     */
    @GetMapping("/pertinentes")
    public ResponseEntity<List<String>> getPertinentes() {
        List<String> strings = service.getStringsByType("PERTINENTE");
        return ResponseEntity.ok(strings);
    }
    
    /**
     * Lista todas as strings de ocorrências não pertinentes
     */
    @GetMapping("/nao-pertinentes")
    public ResponseEntity<List<String>> getNaoPertinentes() {
        List<String> strings = service.getStringsByType("NAO_PERTINENTE");
        return ResponseEntity.ok(strings);
    }
    
    /**
     * Lista todas as strings (pertinentes e não pertinentes)
     */
    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllStrings() {
        List<String> strings = service.getAllStrings();
        return ResponseEntity.ok(strings);
    }
    
    /**
     * Limpa todas as strings de ocorrências pertinentes
     */
    @DeleteMapping("/pertinentes")
    public ResponseEntity<GenericMessage> clearPertinentes() {
        GenericMessage response = service.clearStringsByType("PERTINENTE");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Limpa todas as strings de ocorrências não pertinentes
     */
    @DeleteMapping("/nao-pertinentes")
    public ResponseEntity<GenericMessage> clearNaoPertinentes() {
        GenericMessage response = service.clearStringsByType("NAO_PERTINENTE");
        return ResponseEntity.ok(response);
    }
}
