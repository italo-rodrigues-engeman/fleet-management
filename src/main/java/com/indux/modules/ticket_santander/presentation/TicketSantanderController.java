package com.indux.modules.ticket_santander.presentation;

import com.indux.modules.ticket_santander.application.dtos.*;
import com.indux.modules.ticket_santander.application.services.TicketSantanderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ticket-santander")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TicketSantanderController {
    
    private final TicketSantanderService ticketSantanderService;
    
    @PostMapping(value = "/simple", consumes = "multipart/form-data")
    public ResponseEntity<TicketSantanderResponseDTO> createTicketSimple(
            @RequestParam("funcionarioId") String funcionarioId,
            @RequestParam("numeroAgencia") String numeroAgencia,
            @RequestParam("numeroConta") String numeroConta,
            @RequestParam("carteirinhaAnexos") List<MultipartFile> carteirinhaAnexos) {

        try {
            // Validar que pelo menos um anexo foi enviado
            if (carteirinhaAnexos == null || carteirinhaAnexos.isEmpty() || 
                carteirinhaAnexos.stream().allMatch(file -> file == null || file.isEmpty())) {
                log.warn("Tentativa de criar ticket sem anexos obrigatórios");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .build();
            }
            
            CreateTicketSantanderSimpleRequestDTO request = new CreateTicketSantanderSimpleRequestDTO();
            request.setNumeroAgencia(numeroAgencia);
            request.setNumeroConta(numeroConta);
            request.setCarteirinhaAnexos(carteirinhaAnexos);

            UUID funcionarioUuid = UUID.fromString(funcionarioId);
            TicketSantanderResponseDTO response = ticketSantanderService.createTicketSimple(request, funcionarioUuid);

            // Verificar se é um ticket existente (marcado pelo service)
            if (response.getObservacoes() != null && response.getObservacoes().startsWith("TICKET_EXISTENTE:")) {
                // Remover o marcador das observações
                String observacoesOriginais = response.getObservacoes().substring("TICKET_EXISTENTE: ".length());
                response.setObservacoes(observacoesOriginais.isEmpty() ? null : observacoesOriginais);
                
                log.warn("Conflito ao criar ticket - matrícula já possui ticket existente - ID: {}, Status: {}", 
                        response.getId(), response.getStatus());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Capturar exceções de validação (anexo obrigatório, funcionário não encontrado, etc)
            if (e.getMessage() != null && (e.getMessage().contains("anexo") || e.getMessage().contains("obrigatório"))) {
                log.warn("Erro de validação ao criar ticket: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            log.error("Erro ao criar ticket Santander simples", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Erro ao criar ticket Santander simples", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TicketSantanderResponseDTO> getTicketById(@PathVariable String id) {
        log.info("Recebida requisição para buscar ticket Santander por ID: {}", id);
        
        try {
            TicketSantanderResponseDTO response = ticketSantanderService.getTicketById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Ticket não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao buscar ticket Santander", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    
    @GetMapping
    public ResponseEntity<Page<TicketSantanderResponseDTO>> getAllTickets(
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {
        try {
            Page<TicketSantanderResponseDTO> response = ticketSantanderService.getAllTickets(pageable, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao buscar todos os tickets Santander", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TicketSantanderResponseDTO> updateTicket(@PathVariable String id, 
                                                                  @Valid @RequestBody UpdateTicketSantanderRequestDTO request) {
        log.info("Recebida requisição para atualizar ticket Santander ID: {}", id);
        
        try {
            TicketSantanderResponseDTO response = ticketSantanderService.updateTicket(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Ticket não encontrado para atualização: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao atualizar ticket Santander", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<TicketSantanderResponseDTO> approveTicket(@PathVariable String id,
                                                                  @RequestBody(required = false) ApproveTicketSantanderRequestDTO request) {
        log.info("Recebida requisição para aprovar ticket Santander ID: {}", id);
        
        try {
            TicketSantanderResponseDTO response = ticketSantanderService.approveTicket(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Erro ao aprovar ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro ao aprovar ticket Santander", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<TicketSantanderResponseDTO> rejectTicket(@PathVariable String id,
                                                                  @Valid @RequestBody RejectTicketSantanderRequestDTO request) {
        log.info("Recebida requisição para rejeitar ticket Santander ID: {}", id);
        
        try {
            TicketSantanderResponseDTO response = ticketSantanderService.rejectTicket(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Erro ao rejeitar ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro ao rejeitar ticket Santander", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        log.info("Recebida requisição para deletar ticket Santander ID: {}", id);
        
        try {
            ticketSantanderService.deleteTicket(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Ticket não encontrado para deleção: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao deletar ticket Santander", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}
