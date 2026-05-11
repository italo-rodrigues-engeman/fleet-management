package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ApplyChangeTicketUseCase Tests")
class ApplyChangeTicketUseCaseTest {

    private ChangeTicketRepository changeTicketRepository;
    private PPURepository ppuRepository;
    private ApplyChangeTicketUseCase applyChangeTicketUseCase;
    private JwtAuthenticationToken token;

    @BeforeEach
    void setUp() {
        changeTicketRepository = mock(ChangeTicketRepository.class);
        ppuRepository = mock(PPURepository.class);
        applyChangeTicketUseCase = new ApplyChangeTicketUseCase(changeTicketRepository, ppuRepository);
        token = ChangeTicketFixture.createMockJwtToken();
    }

    @Test
    @DisplayName("Should apply change ticket successfully for service items")
    void shouldApplyServiceChangeTicketSuccessfully() {
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);

        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        assertNotNull(result.getApprovedAt());
        assertEquals(ChangeTicketFixture.USER_ID, result.getApprovedBy());
        assertEquals(ChangeTicketFixture.USER_NAME, result.getApproverName());

        verify(ppuRepository).save(ppu);
        verify(changeTicketRepository).save(result);
        assertEquals(1L, ppu.getVersion());
    }

    @Test
    @DisplayName("Should apply change ticket successfully for equipment items")
    void shouldApplyEquipmentChangeTicketSuccessfully() {
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        pendingTicket.setItem(List.of(ChangeTicketFixture.createEquipmentChangeItem()));
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));


        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);

        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        verify(ppuRepository).save(ppu);
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should apply change ticket successfully for steel cable items")
    void shouldApplySteelCableChangeTicketSuccessfully() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        pendingTicket.setItem(List.of(ChangeTicketFixture.createSteelCableChangeItem()));
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);


        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        verify(ppuRepository).save(ppu);
        verify(changeTicketRepository).save(result);
        assertEquals(ChangeTicketFixture.QUANTITY, ppu.getSteelCables().get(0).getTotalPlanned());
    }

    @Test
    @DisplayName("Should apply change ticket successfully for accessory kit items")
    void shouldApplyAccessoryKitChangeTicketSuccessfully() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        pendingTicket.setItem(List.of(ChangeTicketFixture.createAccessoryKitChangeItem()));
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);

        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        verify(ppuRepository).save(ppu);
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should apply multiple changes in one ticket")
    void shouldApplyMultipleChangesInOneTicket() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        pendingTicket.setItem(List.of(ChangeTicketFixture.createServiceChangeItem(), ChangeTicketFixture.createEquipmentChangeItem(), ChangeTicketFixture.createSteelCableChangeItem(), ChangeTicketFixture.createAccessoryKitChangeItem()));
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);

        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        verify(ppuRepository).save(ppu);
        verify(changeTicketRepository).save(result);
        assertEquals(1L, ppu.getVersion());
    }

    @Test
    @DisplayName("Should throw exception when ticket not found")
    void shouldThrowExceptionWhenTicketNotFound() {

        String ticketId = "nonexistent-ticket";
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ModuleNotFoundFailure exception = assertThrows(ModuleNotFoundFailure.class, () -> applyChangeTicketUseCase.execute(ticketId, token));
        assertEquals("Ticket de mudança não encontrado: " + ticketId, exception.getMessage());
        verify(ppuRepository, never()).save(any());
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when PPU not found")
    void shouldThrowExceptionWhenPPUNotFound() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.empty());

        ModuleNotFoundFailure exception = assertThrows(ModuleNotFoundFailure.class, () -> applyChangeTicketUseCase.execute(ticketId, token));
        assertEquals("PPU não encontrada: " + pendingTicket.getPpuId(), exception.getMessage());
        verify(ppuRepository, never()).save(any());
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ticket is not pending")
    void shouldThrowExceptionWhenTicketIsNotPending() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket appliedTicket = ChangeTicketFixture.createAppliedTicket();

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(appliedTicket));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> applyChangeTicketUseCase.execute(ticketId, token));

        assertTrue(exception.getMessage().contains("Ticket deve estar no status PENDENTE"));
        verify(ppuRepository, never()).findById(any());
        verify(ppuRepository, never()).save(any());
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ticket is rejected")
    void shouldThrowExceptionWhenTicketIsRejected() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket rejectedTicket = ChangeTicketFixture.createRejectedTicket();

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(rejectedTicket));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> applyChangeTicketUseCase.execute(ticketId, token));
        assertTrue(exception.getMessage().contains("Ticket deve estar no status PENDENTE"));
        assertEquals("Status atual: REJECTED", exception.getMessage().substring(exception.getMessage().lastIndexOf("Status atual:")));
    }

    @Test
    @DisplayName("Should throw exception when service not found in PPU")
    void shouldThrowExceptionWhenServiceNotFoundInPPU() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();

        pendingTicket.getItem().get(0).setItemId("non-existent-service");
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));

        ModuleNotFoundFailure exception = assertThrows(ModuleNotFoundFailure.class, () -> applyChangeTicketUseCase.execute(ticketId, token));

        assertTrue(exception.getMessage().contains("Serviço não encontrado na PPU"));
    }

    @Test
    @DisplayName("Should throw exception when platform not found in service")
    void shouldThrowExceptionWhenPlatformNotFoundInService() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();

        pendingTicket.getItem().get(0).setPlatform("NON-EXISTENT-PLATFORM");
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));

        ModuleNotFoundFailure exception = assertThrows(ModuleNotFoundFailure.class, () -> applyChangeTicketUseCase.execute(ticketId, token));
        assertTrue(exception.getMessage().contains("Plataforma não encontrada no serviço"));
    }

    @Test
    @DisplayName("Should handle null version correctly")
    void shouldHandleNullVersionCorrectly() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        pendingTicket.setToVersion(1L);
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(null);

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);

        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        assertEquals(1L, ppu.getVersion());
        verify(ppuRepository).save(ppu);
    }

    @Test
    @DisplayName("Should preserve audit fields when applying ticket")
    void shouldPreserveAuditFieldsWhenApplyingTicket() {

        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        Instant beforeExecution = Instant.now();

        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(ppuRepository.findById(pendingTicket.getPpuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeTicket result = applyChangeTicketUseCase.execute(ticketId, token);

        assertNotNull(result.getApprovedAt());
        assertTrue(result.getApprovedAt().isAfter(beforeExecution) || result.getApprovedAt().equals(beforeExecution));
        assertEquals(ChangeTicketFixture.USER_ID, result.getApprovedBy());
        assertEquals(ChangeTicketFixture.USER_NAME, result.getApproverName());
        assertEquals(pendingTicket.getCreatedAt(), result.getCreatedAt());
        assertEquals(pendingTicket.getCreatedBy(), result.getCreatedBy());
        assertEquals(pendingTicket.getReason(), result.getReason());
    }
}