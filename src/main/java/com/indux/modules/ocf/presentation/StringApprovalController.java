package com.indux.modules.ocf.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ocf.application.dto.SaveStringsRequestDTO;
import com.indux.modules.ocf.application.dto.StringApprovalResponseDTO;
import com.indux.modules.ocf.application.service.StringApprovalService;
import com.indux.modules.ocf.domain.model.StringApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitacoes/ocf/string-approvals")
public class StringApprovalController {
    
    @Autowired
    private StringApprovalService stringApprovalService;
    
    /**
     * Salva uma lista de strings como aprovadas ou rejeitadas
     */
    @PostMapping
    public ResponseEntity<GenericMessage> saveStrings(
            @RequestBody SaveStringsRequestDTO request,
            @RequestParam boolean aprovado,
            JwtAuthenticationToken jwt) {
        
        try {
            if (request.strings() == null || request.strings().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new GenericMessage("Lista de strings não pode estar vazia", 400));
            }
            
            String userId = jwt.getName();
            StringApproval saved = stringApprovalService.saveStrings(request.strings(), aprovado, userId);
            
            String message = aprovado 
                ? "Strings aprovadas salvas com sucesso" 
                : "Strings rejeitadas salvas com sucesso";
            
            return ResponseEntity.ok(new GenericMessage(message, 200));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericMessage("Erro ao salvar strings: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Lista todas as strings salvas
     */
    @GetMapping
    public ResponseEntity<List<StringApprovalResponseDTO>> getAllStringApprovals() {
        try {
            List<StringApproval> approvals = stringApprovalService.getAllStringApprovals();
            List<StringApprovalResponseDTO> response = approvals.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lista apenas strings aprovadas
     */
    @GetMapping("/aprovadas")
    public ResponseEntity<List<String>> getApprovedStrings() {
        try {
            List<StringApproval> approvals = stringApprovalService.getApprovedStrings();
            
            if (!approvals.isEmpty()) {
                // Pega o array aprovado do primeiro registro (mais recente)
                List<String> approvedStrings = approvals.get(0).getAprovado();
                return ResponseEntity.ok(approvedStrings != null ? approvedStrings : List.of());
            } else {
                return ResponseEntity.ok(List.of());
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lista apenas strings rejeitadas
     */
    @GetMapping("/rejeitadas")
    public ResponseEntity<List<String>> getRejectedStrings() {
        try {
            List<StringApproval> approvals = stringApprovalService.getRejectedStrings();
            
            if (!approvals.isEmpty()) {
                // Pega o array rejeitado do primeiro registro (mais recente)
                List<String> rejectedStrings = approvals.get(0).getRejeitado();
                return ResponseEntity.ok(rejectedStrings != null ? rejectedStrings : List.of());
            } else {
                return ResponseEntity.ok(List.of());
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtém o registro mais recente
     */
    @GetMapping("/latest")
    public ResponseEntity<StringApprovalResponseDTO> getLatestRecord() {
        try {
            return stringApprovalService.getLatestRecord()
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Limpa todas as strings
     */
    @DeleteMapping
    public ResponseEntity<GenericMessage> clearAllStrings() {
        try {
            stringApprovalService.clearAllStrings();
            return ResponseEntity.ok(new GenericMessage("Todas as strings foram removidas", 200));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericMessage("Erro ao limpar strings: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Limpa apenas strings aprovadas
     */
    @DeleteMapping("/aprovadas")
    public ResponseEntity<GenericMessage> clearApprovedStrings() {
        try {
            stringApprovalService.clearApprovedStrings();
            return ResponseEntity.ok(new GenericMessage("Strings aprovadas foram removidas", 200));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericMessage("Erro ao limpar strings aprovadas: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Limpa apenas strings rejeitadas
     */
    @DeleteMapping("/rejeitadas")
    public ResponseEntity<GenericMessage> clearRejectedStrings() {
        try {
            stringApprovalService.clearRejectedStrings();
            return ResponseEntity.ok(new GenericMessage("Strings rejeitadas foram removidas", 200));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericMessage("Erro ao limpar strings rejeitadas: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Converte StringApproval para StringApprovalResponseDTO
     */
    private StringApprovalResponseDTO convertToResponseDTO(StringApproval approval) {
        return new StringApprovalResponseDTO(
            approval.getId(),
            approval.getAprovado(),
            approval.getRejeitado()
        );
    }
}
