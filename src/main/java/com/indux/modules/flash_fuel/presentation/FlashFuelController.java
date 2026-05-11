package com.indux.modules.flash_fuel.presentation;

import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.flash_fuel.application.ApplicantFlashFuelService;
import com.indux.modules.flash_fuel.application.FlashFuelService;
import com.indux.modules.flash_fuel.domain.dtos.ApplicantRequest;
import com.indux.modules.flash_fuel.domain.dtos.FlashFuelFilter;
import com.indux.modules.flash_fuel.domain.dtos.FlashFuelRequest;
import com.indux.modules.flash_fuel.domain.entities.ApplicantFlashFuel;
import com.indux.modules.flash_fuel.domain.entities.FlashFuel;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitacoes/flash-combustivel")
public class FlashFuelController {
    private final FlashFuelService service;
    private final ApplicantFlashFuelService applicantFlashService;

    public FlashFuelController(FlashFuelService service, ApplicantFlashFuelService applicantFlashService) {
        this.service = service;
        this.applicantFlashService = applicantFlashService;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> create(@ModelAttribute @Validated(FlashFuelRequest.FirstStep.class) FlashFuelRequest solicitacao, JwtAuthenticationToken jwt) throws IOException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOccurrence(solicitacao, jwt.getName()));
    }

    @PutMapping("/next/{id}")
    public ResponseEntity<GenericMessage> nextStep(@PathVariable String id, @ModelAttribute FlashFuelRequest dto, JwtAuthenticationToken jwt) throws IOException {
        return ResponseEntity.ok(service.moveToNextStep(id, dto, jwt.getName()));
    }

    @PutMapping("/aprove/{id}")
    public ResponseEntity<GenericMessage> aproveAndFinalize(@PathVariable String id, @ModelAttribute @Validated(FlashFuelRequest.FifthStep.class) FlashFuelRequest dto, JwtAuthenticationToken jwt) throws IOException {
        return ResponseEntity.ok(service.finalizeOccurrence(id, dto, jwt.getName()));
    }

    @PutMapping("/batch/status")
    public ResponseEntity<GenericMessage> recuseBatchItens(@RequestBody BatchStatusDTO body, JwtAuthenticationToken jwt) {
        service.batchUpdateStatus(body, UUID.fromString(jwt.getName()));
        return ResponseEntity.ok(new GenericMessage("Ocorrências Rejeitadas com sucesso.", 200));
    }
    @GetMapping("/get/me")
    public ResponseEntity<Page<FlashFuel>> getMyOccurrence(JwtAuthenticationToken jwt, Pageable pageable) {
        return ResponseEntity.ok(service.listOccurrencesByUser(UUID.fromString(jwt.getName()), pageable));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<FlashFuel> getOccurrence(@PathVariable String id) {
        return ResponseEntity.ok(service.getOccurrence(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<FlashFuel>> listCurrentOccurrence(
            JwtAuthenticationToken jwt,
            Pageable pageable,
            @RequestParam(required = false, name = "full", defaultValue = "false") boolean full,
            @RequestParam(required = false, name = "filial") Integer filial
    ) {
        if (full) return ResponseEntity.ok(service.listAllAllowed(UUID.fromString(jwt.getName()), pageable));
        if (filial != null) {
            return ResponseEntity.ok(service.listOccurrencesByFilial(filial, pageable));
        }
        return ResponseEntity.ok(service.listOccurrencesAllowed(UUID.fromString(jwt.getName()), pageable));
    }

    @PutMapping("/recuse/{id}")
    public ResponseEntity<GenericMessage> recuseOccurrence(@PathVariable String id, @RequestBody @Nullable FlashFuelRequest dto, JwtAuthenticationToken jwt) throws MessagingException {
        GenericMessage message = service.rejectOccurrence(id, dto, jwt.getName());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<GenericMessage> requestReview(@PathVariable String id, @ModelAttribute FlashFuelRequest body, JwtAuthenticationToken jwt) throws MessagingException {
        return ResponseEntity.ok(service.requestReview(id, body, jwt.getName()));
    }

    @PutMapping("/reviewed/{id}")
    public ResponseEntity<GenericMessage> sendDataReviewed(@PathVariable String id, @ModelAttribute FlashFuelRequest body, JwtAuthenticationToken jwt, @RequestParam(required = false, name = "edit", defaultValue = "false") boolean edit) throws IOException {
        return ResponseEntity.ok(service.editAfterReview(id, body, jwt.getName(), edit));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<FlashFuel>> searchGet(
            FlashFuelFilter filter,
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



    // -- Aplicantes - Requerentes -- //
    @GetMapping("/applicants")
    public ResponseEntity<List<ApplicantFlashFuel>> getApplicants() {
        var list = applicantFlashService.findAll();
        if(list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }


    @PatchMapping("/applicants/{id}")
    public ResponseEntity<?> updateApplicant(
            @PathVariable Long id,
            @RequestBody Map<String, Object> applicant
    ) {
        Object tetoValorObj = applicant.get("tetoValor");
        BigDecimal tetoValor = null;

        if (tetoValorObj instanceof String) {
            tetoValor = new BigDecimal((String) tetoValorObj);
        } else if (tetoValorObj instanceof Number) {
            tetoValor = BigDecimal.valueOf(((Number) tetoValorObj).doubleValue());
        } else {
            throw new IllegalArgumentException("Valor inválido para tetoValor");
        }

        ApplicantFlashFuel updatedApplicant = applicantFlashService.update(id, tetoValor);
        return ResponseEntity.ok(updatedApplicant);
    }

    @DeleteMapping("/applicants/{id}")
    public ResponseEntity<GenericMessage> deleteApplicant(@PathVariable Long id) {
        applicantFlashService.deleteById(id);
        return ResponseEntity.ok(new GenericMessage("Requerente excluído com sucesso.", 200));
    }

    @PostMapping("/applicants/create")
    public ResponseEntity<GenericMessage> createApplicant(@RequestBody @Validated  ApplicantRequest applicant) {
        applicantFlashService.save(applicant);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericMessage("Requerente criado com sucesso.", 201));
    }

}
