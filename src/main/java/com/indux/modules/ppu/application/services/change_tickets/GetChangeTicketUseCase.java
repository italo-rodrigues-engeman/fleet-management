package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import com.indux.modules.ppu.infra.mapper.ticket.ChangeTicketResponseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class GetChangeTicketUseCase {
    private final ChangeTicketRepository repository;
    private final ChangeTicketResponseMapper mapper;
    
    public GetChangeTicketUseCase(ChangeTicketRepository repository, ChangeTicketResponseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    public Page<ChangeTicketGridProjection> execute(Pageable pageable) {
        return repository.findAllBy(pageable);
    }
    
    public Page<ChangeTicketGridProjection> executeByPpuId(String ppuId, Pageable pageable) {
        return repository.findByPpuId(ppuId, pageable);
    }

        public ChangeTicketResponseDTO executeById(String id) {
        var ticket = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Ticket não encontrado: " + id));
        
        return mapper.toResponseDTO(ticket);
    }
}