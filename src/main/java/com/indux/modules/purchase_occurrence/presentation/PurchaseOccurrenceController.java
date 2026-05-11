package com.indux.modules.purchase_occurrence.presentation;

import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.generic.GenericMessageWithCode;
import com.indux.core.domain.service.occurrence.AbstractOccurrenceService;
import com.indux.modules.purchase_occurrence.application.PurchaseOccurrenceService;
import com.indux.modules.purchase_occurrence.domain.dto.PurchaseOccurrenceFilter;
import com.indux.modules.purchase_occurrence.domain.dto.PurchaseRequest;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrence;
import com.mongodb.lang.Nullable;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitacoes/purchase-occurrence")
public class PurchaseOccurrenceController {
    private final AbstractOccurrenceService<PurchaseOccurrence> service;
    private final PurchaseOccurrenceService purchaseService;

    public PurchaseOccurrenceController(PurchaseOccurrenceService service) {
        this.service = service;
        this.purchaseService = service;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessageWithCode> createFirstStep(@ModelAttribute @Validated(PurchaseRequest.FirstStep.class) PurchaseRequest create, JwtAuthenticationToken token) throws InterruptedException {
        return ResponseEntity.ok(purchaseService.createPurchaseOccurrence(create, token.getName()));
    }

    @PutMapping("/batch/status")
    public ResponseEntity<GenericMessage> recuseBatchItens(@RequestBody BatchStatusDTO body, JwtAuthenticationToken jwt) {
        service.batchUpdateStatus(body, UUID.fromString(jwt.getName()));
        return ResponseEntity.ok(new GenericMessage("Ocorrências Rejeitadas com sucesso.", 200));
    }

    @GetMapping("/get/me")
    public ResponseEntity<Page<PurchaseOccurrence>> getMyOccurrence(JwtAuthenticationToken jwt, Pageable pageable) {
        return ResponseEntity.ok(service.listOccurrencesByUser(UUID.fromString(jwt.getName()), pageable));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<PurchaseOccurrence> getOccurrence(@PathVariable String id) {
        return ResponseEntity.ok(service.getOccurrence(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PurchaseOccurrence>> listCurrentOccurrence(
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC)
            JwtAuthenticationToken jwt,
            Pageable pageable,
            @RequestParam(required = false, name = "full", defaultValue = "false") boolean full) {
        if (full) return ResponseEntity.ok(service.listAllAllowed(UUID.fromString(jwt.getName()), pageable));
        return ResponseEntity.ok(service.listOccurrencesAllowed(UUID.fromString(jwt.getName()), pageable));
    }

    @PutMapping("/aprove/{id}")
    public ResponseEntity<GenericMessage> aproveStepTwo(@PathVariable String id, @ModelAttribute @Validated(PurchaseRequest.SecondStep.class)  PurchaseRequest dto, JwtAuthenticationToken jwt) throws IOException {
        return ResponseEntity.ok(service.finalizeOccurrence(id, dto, jwt.getName()));
    }

    @PutMapping("/recuse/{id}")
    public ResponseEntity<GenericMessage> recuseOccurrence(@PathVariable String id, @RequestBody @Nullable PurchaseRequest dto, JwtAuthenticationToken jwt) throws MessagingException {
        GenericMessage message = service.rejectOccurrence(id, dto, jwt.getName());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<GenericMessage> requestReview(@PathVariable String id, @RequestBody PurchaseRequest body, JwtAuthenticationToken jwt) throws MessagingException {
        return ResponseEntity.ok(service.requestReview(id, body, jwt.getName()));
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<GenericMessage> sendDataReviewed(@PathVariable String id, @ModelAttribute PurchaseRequest body, JwtAuthenticationToken jwt, @RequestParam(required = false, name = "edit", defaultValue = "false") boolean edit) throws IOException {
        return ResponseEntity.ok(service.editAfterReview(id, body, jwt.getName(), edit));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<PurchaseOccurrence>> searchGet(
            PurchaseOccurrenceFilter filter,
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC)
            JwtAuthenticationToken jwt,
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

}
