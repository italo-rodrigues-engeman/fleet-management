package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class RejectChangeTicketUseCase {
    private final ChangeTicketRepository repository;
    
    public RejectChangeTicketUseCase(ChangeTicketRepository repository) {
        this.repository = repository;
    }

    public ChangeTicket execute(String ticketId, String rejectionReason, JwtAuthenticationToken token) {
        var ticket = repository.findById(ticketId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Change ticket não encontrado: " + ticketId));
        
        if (ticket.getStatus() != ChangeTicket.TicketStatus.PENDING) {
            throw new IllegalStateException("Ticket deve estar no status PENDING para ser rejeitado. Status atual: " + ticket.getStatus());
        }
        
        ticket.setStatus(ChangeTicket.TicketStatus.REJECTED);
        ticket.setApprovedAt(Instant.now());
        ticket.setApprovedBy(token.getName());
        ticket.setApproverName(token.getToken().getClaimAsString("name"));
        ticket.setRejectionReason(rejectionReason);
        
        return repository.save(ticket);
    }
}