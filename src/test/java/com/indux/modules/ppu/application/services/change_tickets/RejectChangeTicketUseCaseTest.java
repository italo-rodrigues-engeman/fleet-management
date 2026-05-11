package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RejectChangeTicketUseCaseTest {

    private ChangeTicketRepository changeTicketRepository;
    private RejectChangeTicketUseCase rejectChangeTicketUseCase;
    private JwtAuthenticationToken token;

    @BeforeEach
    void setUp() {
        changeTicketRepository = mock(ChangeTicketRepository.class);
        rejectChangeTicketUseCase = new RejectChangeTicketUseCase(changeTicketRepository);
        token = ChangeTicketFixture.createMockJwtToken();
    }

    @Test
    @DisplayName("Should reject change ticket successfully")
    void shouldRejectChangeTicketSuccessfully() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertEquals(rejectionReason, result.getRejectionReason());
        assertNotNull(result.getApprovedAt());
        assertEquals(ChangeTicketFixture.USER_ID, result.getApprovedBy());
        assertEquals(ChangeTicketFixture.USER_NAME, result.getApproverName());
        
        verify(changeTicketRepository).findById(ticketId);
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should preserve original ticket data when rejecting")
    void shouldPreserveOriginalTicketDataWhenRejecting() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        Instant originalCreatedAt = pendingTicket.getCreatedAt();
        String originalCreatedBy = pendingTicket.getCreatedBy();
        String originalReason = pendingTicket.getReason();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(originalCreatedAt, result.getCreatedAt());
        assertEquals(originalCreatedBy, result.getCreatedBy());
        assertEquals(originalReason, result.getReason());
        assertEquals(pendingTicket.getPpuId(), result.getPpuId());
        assertEquals(pendingTicket.getFromVersion(), result.getFromVersion());
        assertEquals(pendingTicket.getToVersion(), result.getToVersion());
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should set approval timestamp when rejecting")
    void shouldSetApprovalTimestampWhenRejecting() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        Instant beforeExecution = Instant.now();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result.getApprovedAt());
        assertTrue(result.getApprovedAt().isAfter(beforeExecution) || result.getApprovedAt().equals(beforeExecution));
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should throw exception when ticket not found")
    void shouldThrowExceptionWhenTicketNotFound() {
        
        String ticketId = "non-existent-ticket";
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.empty());

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)
        );
        
        assertEquals("Change ticket não encontrado: " + ticketId, exception.getMessage());
        verify(changeTicketRepository).findById(ticketId);
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ticket is already applied")
    void shouldThrowExceptionWhenTicketIsAlreadyApplied() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket appliedTicket = ChangeTicketFixture.createAppliedTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(appliedTicket));

        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)
        );
        
        assertTrue(exception.getMessage().contains("Ticket deve estar no status PENDING"));
        assertTrue(exception.getMessage().contains("Status atual: APPLIED"));
        verify(changeTicketRepository).findById(ticketId);
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ticket is already rejected")
    void shouldThrowExceptionWhenTicketIsAlreadyRejected() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket rejectedTicket = ChangeTicketFixture.createRejectedTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(rejectedTicket));

        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)
        );
        
        assertTrue(exception.getMessage().contains("Ticket deve estar no status PENDING"));
        assertTrue(exception.getMessage().contains("Status atual: REJECTED"));
        verify(changeTicketRepository).findById(ticketId);
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null rejection reason")
    void shouldHandleNullRejectionReason() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = null;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertNull(result.getRejectionReason());
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should handle empty rejection reason")
    void shouldHandleEmptyRejectionReason() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = "";
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertEquals("", result.getRejectionReason());
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should handle long rejection reason")
    void shouldHandleLongRejectionReason() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String longRejectionReason = "Esta é uma razão de rejeição muito longa que contém muitos detalhes sobre por que o ticket foi rejeitado. ".repeat(10);
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, longRejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertEquals(longRejectionReason, result.getRejectionReason());
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should extract user information from JWT token correctly")
    void shouldExtractUserInformationFromJwtTokenCorrectly() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicketFixture.USER_ID, result.getApprovedBy());
        assertEquals(ChangeTicketFixture.USER_NAME, result.getApproverName());
        
        verify(token).getName();
        verify(token.getToken()).getClaimAsString("name");
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should handle special characters in rejection reason")
    void shouldHandleSpecialCharactersInRejectionReason() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = "Razão com caracteres especiais: áéíóú ñç @#$%&*()[]{}";
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertEquals(rejectionReason, result.getRejectionReason());
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should not modify other ticket fields when rejecting")
    void shouldNotModifyOtherTicketFieldsWhenRejecting() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        
        String originalId = pendingTicket.getId();
        String originalPpuId = pendingTicket.getPpuId();
        Long originalFromVersion = pendingTicket.getFromVersion();
        Long originalToVersion = pendingTicket.getToVersion();
        var originalItem = pendingTicket.getItem();
        var originalDiff = pendingTicket.getDiff();
        var originalActor = pendingTicket.getActor();
        var originalSource = pendingTicket.getSource();
        var originalContract = pendingTicket.getContract();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(originalId, result.getId());
        assertEquals(originalPpuId, result.getPpuId());
        assertEquals(originalFromVersion, result.getFromVersion());
        assertEquals(originalToVersion, result.getToVersion());
        assertEquals(originalItem, result.getItem());
        assertEquals(originalDiff, result.getDiff());
        assertEquals(originalActor, result.getActor());
        assertEquals(originalSource, result.getSource());
        assertEquals(originalContract, result.getContract());
        
        
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertNotNull(result.getApprovedAt());
        assertEquals(ChangeTicketFixture.USER_ID, result.getApprovedBy());
        assertEquals(ChangeTicketFixture.USER_NAME, result.getApproverName());
        assertEquals(rejectionReason, result.getRejectionReason());
        
        verify(changeTicketRepository).save(result);
    }

    @Test
    @DisplayName("Should verify repository save is called exactly once")
    void shouldVerifyRepositorySaveIsCalledExactlyOnce() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        verify(changeTicketRepository, times(1)).findById(ticketId);
        verify(changeTicketRepository, times(1)).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should return the same ticket instance that was saved")
    void shouldReturnTheSameTicketInstanceThatWasSaved() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket pendingTicket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(pendingTicket));
        when(changeTicketRepository.save(pendingTicket)).thenReturn(pendingTicket);

        
        ChangeTicket result = rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token);

        
        assertSame(pendingTicket, result);
        verify(changeTicketRepository).save(pendingTicket);
    }
}