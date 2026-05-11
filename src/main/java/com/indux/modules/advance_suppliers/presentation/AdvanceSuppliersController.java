package com.indux.modules.advance_suppliers.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.advance_suppliers.application.service.AdvanceSupplierService;
import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersFilter;
import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersRequest;
import com.indux.modules.advance_suppliers.domain.entities.AdvanceSuppliers;
import com.indux.modules.advance_suppliers.infra.ocr.Ocr;
import com.indux.modules.advance_suppliers.infra.ocr.OcrNotaFiscalResponse;
import com.mongodb.lang.Nullable;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitacoes/adiantamento-fornecedores")
public class AdvanceSuppliersController {
    private final AdvanceSupplierService service;
    private final Ocr ocrService;

    public AdvanceSuppliersController(AdvanceSupplierService service, Ocr ocrService) {
        this.service = service;
        this.ocrService = ocrService;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> create(@ModelAttribute @Validated(AdvanceSuppliersRequest.FirstStep.class) AdvanceSuppliersRequest solicitacao, JwtAuthenticationToken jwt) throws IOException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOccurrence(solicitacao, jwt.getName()));
    }

    @PutMapping("/next/{id}")
    public ResponseEntity<GenericMessage> nextStep(@PathVariable String id, @ModelAttribute AdvanceSuppliersRequest dto, JwtAuthenticationToken jwt) throws IOException {
        return ResponseEntity.ok(service.moveToNextStep(id, dto, jwt.getName()));
    }

    @PutMapping("/aprove/{id}")
    public ResponseEntity<GenericMessage> aproveAndFinalize(@PathVariable String id, @ModelAttribute AdvanceSuppliersRequest dto, JwtAuthenticationToken jwt) throws IOException {
        return ResponseEntity.ok(service.finalizeOccurrence(id, dto, jwt.getName()));
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<GenericMessage> requestReview(@PathVariable String id, @ModelAttribute AdvanceSuppliersRequest body, JwtAuthenticationToken jwt) throws MessagingException {
        return ResponseEntity.ok(service.requestReview(id, body, jwt.getName()));
    }

    @PutMapping("/reviewed/{id}")
    public ResponseEntity<?> sendDataReviewed(@PathVariable String id, @ModelAttribute AdvanceSuppliersRequest body, JwtAuthenticationToken jwt, @RequestParam(required = false, name = "edit", defaultValue = "false") boolean edit) throws IOException {
        try {
            // Primeiro chamamos o serviço para atualizar a ocorrência
            GenericMessage message = service.editAfterReview(id, body, jwt.getName(), edit);
            
            // Em seguida, buscamos a ocorrência atualizada para retornar ao cliente
            AdvanceSuppliers updatedOccurrence = service.getOccurrence(id);
            
            // Retornamos a ocorrência atualizada
            return ResponseEntity.ok(updatedOccurrence);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro ao processar revisão: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AdvanceSuppliers> getOccurrence(@PathVariable String id) {
        return ResponseEntity.ok(service.getOccurrence(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AdvanceSuppliers>> listCurrentOccurrence(
            JwtAuthenticationToken jwt,
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestParam(required = false, name = "full", defaultValue = "false") boolean full) {
        if (full) return ResponseEntity.ok(service.listAllAllowed(UUID.fromString(jwt.getName()), pageable));
        return ResponseEntity.ok(service.listOccurrencesAllowed(UUID.fromString(jwt.getName()), pageable));
    }

    @GetMapping("/get/me")
    public ResponseEntity<Page<AdvanceSuppliers>> getMyOccurrence(JwtAuthenticationToken jwt, Pageable pageable) {
        return ResponseEntity.ok(service.listOccurrencesByUser(UUID.fromString(jwt.getName()), pageable));
    }

    @PutMapping("/recuse/{id}")
    public ResponseEntity<GenericMessage> recuseOccurrence(@PathVariable String id, @RequestBody @Nullable AdvanceSuppliersRequest dto, JwtAuthenticationToken jwt) throws MessagingException {
        GenericMessage message = service.rejectOccurrence(id, dto, jwt.getName());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<AdvanceSuppliers>> searchGet(
            AdvanceSuppliersFilter filter,
            JwtAuthenticationToken jwt,
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {

        boolean hasPermission = jwt.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role ->
                        "ROLE_ADMINISTRADOR".equals(role) ||
                                "ROLE_DESENVOLVEDOR".equals(role)
                );
        return ResponseEntity.ok(
                service.searchFilter(filter, hasPermission, jwt.getName(), pageable));
    }

    @PostMapping("/ocr/process")
    public ResponseEntity<?> processOcr(@RequestParam("arquivo") MultipartFile file, JwtAuthenticationToken jwt) {
        try {
            // Validações básicas
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new GenericMessage("Arquivo não pode estar vazio", 400));
            }

            // Verificação de tipo de arquivo (PDF, imagens)
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                return ResponseEntity.badRequest()
                        .body(new GenericMessage("Tipo de arquivo não suportado. Apenas imagens e PDFs são aceitos.", 400));
            }

            // Chama o serviço OCR
            OcrNotaFiscalResponse response = ocrService.call(
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    contentType
            );

            // Verifica se a resposta foi recebida com sucesso
            if (response != null && response.isProcessado()) {
                return ResponseEntity.ok(response);
            } else {
                String errorMessage;
                if (response == null) {
                    errorMessage = "Erro na comunicação com o microserviço";
                } else {
                    errorMessage = response.getMensagem() != null ? response.getMensagem() : 
                                  "Documento não foi processado corretamente";
                    if (response.getErro() != null) {
                        errorMessage += " - Erro: " + response.getErro();
                    }
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new GenericMessage("Erro ao processar OCR: " + errorMessage, 500));
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro ao processar arquivo: " + e.getMessage(), 500));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro inesperado: " + e.getMessage(), 500));
        }
    }
}
