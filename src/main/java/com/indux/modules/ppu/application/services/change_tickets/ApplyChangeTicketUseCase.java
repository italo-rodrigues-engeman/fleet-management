package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Transactional
public class ApplyChangeTicketUseCase {
    private final ChangeTicketRepository repository;
    private final PPURepository ppuRepository;
    
    public ApplyChangeTicketUseCase(ChangeTicketRepository repository, PPURepository ppuRepository) {
        this.repository = repository;
        this.ppuRepository = ppuRepository;
    }

    public ChangeTicket execute(String ticketId, JwtAuthenticationToken token) {
        var ticket = repository.findById(ticketId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Ticket de mudança não encontrado: " + ticketId));
        
        if (ticket.getStatus() != ChangeTicket.TicketStatus.PENDING) {
            throw new IllegalStateException("Ticket deve estar no status PENDENTE para ser aplicado. Status atual: " + ticket.getStatus());
        }
        
        var ppu = ppuRepository.findById(ticket.getPpuId())
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada: " + ticket.getPpuId()));
        
        for (var changeItem : ticket.getItem()) {
            applyChangeItemToPPU(ppu, changeItem);
        }
        
        ppu.setVersion(ticket.getToVersion());
        ppuRepository.save(ppu);
        
        ticket.setStatus(ChangeTicket.TicketStatus.APPLIED);
        ticket.setApprovedAt(Instant.now());
        ticket.setApprovedBy(token.getName());
        ticket.setApproverName(token.getToken().getClaimAsString("name"));
        
        return repository.save(ticket);
    }
    

    private void applyChangeItemToPPU(PPUEntity ppu, ChangeTicket.ChangeItem changeItem) {
        switch (changeItem.getLineType()) {
            case SERVICE -> applyServiceChange(ppu, changeItem);
            case EQUIPMENT -> applyEquipmentChange(ppu, changeItem);
            case STEEL_CABLE -> applySteelCableChange(ppu, changeItem);
            case ACCESSORY_KIT -> applyAccessoryKitChange(ppu, changeItem);
            default -> throw new IllegalArgumentException("Tipo de linha não suportado: " + changeItem.getLineType());
        }
    }
    

    private void applyServiceChange(PPUEntity ppu, ChangeTicket.ChangeItem changeItem) {
        var service = ppu.getServices().stream()
                .filter(s -> s.getId().equals(changeItem.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Serviço não encontrado na PPU: " + changeItem.getItemId()));
        
        var forecast = service.getMeasurementForecasts().stream()
                .filter(f -> f.getPlatform().equals(changeItem.getPlatform()))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Plataforma não encontrada no serviço: " + changeItem.getPlatform()));
        
        forecast.setTotal(changeItem.getQuantity());
    }
    

    private void applyEquipmentChange(PPUEntity ppu, ChangeTicket.ChangeItem changeItem) {
        var equipment = ppu.getEquipments().stream()
                .filter(e -> e.getId().equals(changeItem.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Equipamento não encontrado na PPU: " + changeItem.getItemId()));
        
        if (changeItem.getPlatform() != null) {
            var forecast = equipment.getMeasurementForecasts().stream()
                    .filter(f -> f.getPlatform().equals(changeItem.getPlatform()))
                    .findFirst()
                    .orElseThrow(() -> new ModuleNotFoundFailure("Plataforma não encontrada no equipamento: " + changeItem.getPlatform()));
            
            forecast.setTotal(changeItem.getQuantity());
        }
    }
    
    private void applySteelCableChange(PPUEntity ppu, ChangeTicket.ChangeItem changeItem) {
        var steelCable = ppu.getSteelCables().stream()
                .filter(sc -> sc.getId().equals(changeItem.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Cabo de aço não encontrado na PPU: " + changeItem.getItemId()));
        
        steelCable.setTotalPlanned(changeItem.getQuantity());
    }
    
    private void applyAccessoryKitChange(PPUEntity ppu, ChangeTicket.ChangeItem changeItem) {
        var accessoryKit = ppu.getAccessoryKits().stream()
                .filter(ak -> ak.getId().equals(changeItem.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Kit de acessório não encontrado na PPU: " + changeItem.getItemId()));
        
        if (changeItem.getPlatform() != null) {
            var forecast = accessoryKit.getMeasurementForecasts().stream()
                    .filter(f -> f.getPlatform().equals(changeItem.getPlatform()))
                    .findFirst()
                    .orElseThrow(() -> new ModuleNotFoundFailure("Plataforma não encontrada no kit de acessório: " + changeItem.getPlatform()));
            
            forecast.setTotal(changeItem.getQuantity());
        }
    }
}