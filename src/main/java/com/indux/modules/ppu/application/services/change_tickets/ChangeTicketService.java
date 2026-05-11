package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class ChangeTicketService {
    private final CreateChangeTicketUseCase createChangeTicketUseCase;
    private final GetChangeTicketUseCase getChangeTicketUseCase;
    private final ApplyChangeTicketUseCase applyChangeTicketUseCase;
    private final RejectChangeTicketUseCase rejectChangeTicketUseCase;

    public ChangeTicketService(
            CreateChangeTicketUseCase createChangeTicketUseCase,
            GetChangeTicketUseCase getChangeTicketUseCase,
            ApplyChangeTicketUseCase applyChangeTicketUseCase,
            RejectChangeTicketUseCase rejectChangeTicketUseCase) {
        this.createChangeTicketUseCase = createChangeTicketUseCase;
        this.getChangeTicketUseCase = getChangeTicketUseCase;
        this.applyChangeTicketUseCase = applyChangeTicketUseCase;
        this.rejectChangeTicketUseCase = rejectChangeTicketUseCase;
    }

    public ChangeTicket createTicket(ChangeTicketRequest request, HttpServletRequest req, JwtAuthenticationToken token) {
        return createChangeTicketUseCase.execute(request, req, token);
    }

    public Page<ChangeTicketGridProjection> getChangeTicketsGrid(Pageable pageable) {
        return getChangeTicketUseCase.execute(pageable);
    }

    public ChangeTicketResponseDTO getChangeTicketById(String id) {
        return getChangeTicketUseCase.executeById(id);
    }

    public ChangeTicket applyChangeTicket(String ticketId, JwtAuthenticationToken token) {
        return applyChangeTicketUseCase.execute(ticketId, token);
    }

    public ChangeTicket rejectChangeTicket(String ticketId, String rejectionReason, JwtAuthenticationToken token) {
        return rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);
    }
}