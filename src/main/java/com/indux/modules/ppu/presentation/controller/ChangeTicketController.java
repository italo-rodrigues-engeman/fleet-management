package com.indux.modules.ppu.presentation.controller;

import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.application.dtos.requests.RejectChangeTicketRequest;
import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.application.services.change_tickets.ChangeTicketService;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitacoes/ppu/change-tickets")
public class ChangeTicketController {
    private final ChangeTicketService service;

    public ChangeTicketController(ChangeTicketService service) {
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<ChangeTicket> create(@RequestBody @Validated ChangeTicketRequest request, JwtAuthenticationToken token, HttpServletRequest req){
        return ResponseEntity.ok(service.createTicket(request, req, token));
    }

    @GetMapping("/fetch")
    public ResponseEntity<Page<ChangeTicketGridProjection>> getGrid(Pageable pageable) {
        return ResponseEntity.ok(service.getChangeTicketsGrid(pageable));
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<ChangeTicketResponseDTO> getGridByPpuId(
            @PathVariable String id,
            Pageable pageable) {
        return ResponseEntity.ok(service.getChangeTicketById(id));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<ChangeTicket> apply(@PathVariable String id, JwtAuthenticationToken token) {
        return ResponseEntity.ok(service.applyChangeTicket(id, token));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ChangeTicket> reject(
            @PathVariable String id,
            @RequestBody @Validated RejectChangeTicketRequest request,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(service.rejectChangeTicket(id, request.motivoRejeicao(), token));
    }
}