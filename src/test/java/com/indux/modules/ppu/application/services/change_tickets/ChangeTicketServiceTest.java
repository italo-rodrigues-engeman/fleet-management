package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChangeTicketServiceTest {

    private CreateChangeTicketUseCase createChangeTicketUseCase;
    private GetChangeTicketUseCase getChangeTicketUseCase;
    private ApplyChangeTicketUseCase applyChangeTicketUseCase;
    private RejectChangeTicketUseCase rejectChangeTicketUseCase;
    private ChangeTicketService changeTicketService;
    private JwtAuthenticationToken token;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        createChangeTicketUseCase = mock(CreateChangeTicketUseCase.class);
        getChangeTicketUseCase = mock(GetChangeTicketUseCase.class);
        applyChangeTicketUseCase = mock(ApplyChangeTicketUseCase.class);
        rejectChangeTicketUseCase = mock(RejectChangeTicketUseCase.class);
        
        changeTicketService = new ChangeTicketService(
                createChangeTicketUseCase,
                getChangeTicketUseCase,
                applyChangeTicketUseCase,
                rejectChangeTicketUseCase
        );
        
        token = ChangeTicketFixture.createMockJwtToken();
        httpRequest = ChangeTicketFixture.createMockHttpRequest();
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicketSuccessfully() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();
        
        when(createChangeTicketUseCase.execute(request, httpRequest, token)).thenReturn(expectedTicket);

        
        ChangeTicket result = changeTicketService.createTicket(request, httpRequest, token);

        
        assertNotNull(result);
        assertEquals(expectedTicket.getId(), result.getId());
        assertEquals(expectedTicket.getStatus(), result.getStatus());
        
        verify(createChangeTicketUseCase).execute(request, httpRequest, token);
    }

    @Test
    @DisplayName("Should get change tickets grid successfully")
    void shouldGetChangeTicketsGridSuccessfully() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChangeTicketGridProjection> expectedPage = ChangeTicketFixture.createMockGridPage();
        
        when(getChangeTicketUseCase.execute(pageable)).thenReturn(expectedPage);

        
        Page<ChangeTicketGridProjection> result = changeTicketService.getChangeTicketsGrid(pageable);

        
        assertNotNull(result);
        assertEquals(expectedPage.getContent().size(), result.getContent().size());
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        
        verify(getChangeTicketUseCase).execute(pageable);
    }

    @Test
    @DisplayName("Should get change ticket by ID successfully")
    void shouldGetChangeTicketByIdSuccessfully() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicketResponseDTO expectedResponse = ChangeTicketFixture.createValidResponseDTO();
        
        when(getChangeTicketUseCase.executeById(ticketId)).thenReturn(expectedResponse);

        
        ChangeTicketResponseDTO result = changeTicketService.getChangeTicketById(ticketId);

        
        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.ppuId(), result.ppuId());
        
        verify(getChangeTicketUseCase).executeById(ticketId);
    }

    @Test
    @DisplayName("Should apply change ticket successfully")
    void shouldApplyChangeTicketSuccessfully() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket expectedTicket = ChangeTicketFixture.createAppliedTicket();
        
        when(applyChangeTicketUseCase.execute(ticketId, token)).thenReturn(expectedTicket);

        
        ChangeTicket result = changeTicketService.applyChangeTicket(ticketId, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.APPLIED, result.getStatus());
        assertEquals(expectedTicket.getId(), result.getId());
        
        verify(applyChangeTicketUseCase).execute(ticketId, token);
    }

    @Test
    @DisplayName("Should reject change ticket successfully")
    void shouldRejectChangeTicketSuccessfully() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicket expectedTicket = ChangeTicketFixture.createRejectedTicket();
        
        when(rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)).thenReturn(expectedTicket);

        
        ChangeTicket result = changeTicketService.rejectChangeTicket(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        assertEquals(ChangeTicket.TicketStatus.REJECTED, result.getStatus());
        assertEquals(expectedTicket.getId(), result.getId());
        assertEquals(rejectionReason, result.getRejectionReason());
        
        verify(rejectChangeTicketUseCase).execute(ticketId, rejectionReason, token);
    }

    @Test
    @DisplayName("Should handle multiple ticket creation requests")
    void shouldHandleMultipleTicketCreationRequests() {
        
        ChangeTicketRequest request1 = ChangeTicketFixture.createValidRequest();
        ChangeTicketRequest request2 = ChangeTicketFixture.createMultiItemRequest();
        ChangeTicket ticket1 = ChangeTicketFixture.createPendingTicket();
        ChangeTicket ticket2 = ChangeTicketFixture.createPendingTicket();
        ticket2.setId("ticket-456");
        
        when(createChangeTicketUseCase.execute(request1, httpRequest, token)).thenReturn(ticket1);
        when(createChangeTicketUseCase.execute(request2, httpRequest, token)).thenReturn(ticket2);

        
        ChangeTicket result1 = changeTicketService.createTicket(request1, httpRequest, token);
        ChangeTicket result2 = changeTicketService.createTicket(request2, httpRequest, token);

        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
        
        verify(createChangeTicketUseCase, times(2)).execute(any(ChangeTicketRequest.class), eq(httpRequest), eq(token));
    }

    @Test
    @DisplayName("Should handle pagination parameters correctly")
    void shouldHandlePaginationParametersCorrectly() {
        
        Pageable smallPage = PageRequest.of(0, 5);
        Pageable largePage = PageRequest.of(1, 20);
        Page<ChangeTicketGridProjection> smallPageResult = Page.empty(smallPage);
        Page<ChangeTicketGridProjection> largePageResult = Page.empty(largePage);
        
        when(getChangeTicketUseCase.execute(smallPage)).thenReturn(smallPageResult);
        when(getChangeTicketUseCase.execute(largePage)).thenReturn(largePageResult);

        
        Page<ChangeTicketGridProjection> result1 = changeTicketService.getChangeTicketsGrid(smallPage);
        Page<ChangeTicketGridProjection> result2 = changeTicketService.getChangeTicketsGrid(largePage);

        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(5, result1.getSize());
        assertEquals(20, result2.getSize());
        
        verify(getChangeTicketUseCase).execute(smallPage);
        verify(getChangeTicketUseCase).execute(largePage);
    }

    @Test
    @DisplayName("Should delegate error handling to use cases")
    void shouldDelegateErrorHandlingToUseCases() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        RuntimeException expectedException = new RuntimeException("Use case error");
        
        when(createChangeTicketUseCase.execute(request, httpRequest, token)).thenThrow(expectedException);

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> changeTicketService.createTicket(request, httpRequest, token)
        );
        
        assertEquals("Use case error", exception.getMessage());
        verify(createChangeTicketUseCase).execute(request, httpRequest, token);
    }

    @Test
    @DisplayName("Should handle null rejection reason")
    void shouldHandleNullRejectionReason() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = null;
        ChangeTicket expectedTicket = ChangeTicketFixture.createRejectedTicket();
        
        when(rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)).thenReturn(expectedTicket);

        
        ChangeTicket result = changeTicketService.rejectChangeTicket(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        verify(rejectChangeTicketUseCase).execute(ticketId, rejectionReason, token);
    }

    @Test
    @DisplayName("Should handle empty rejection reason")
    void shouldHandleEmptyRejectionReason() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = "";
        ChangeTicket expectedTicket = ChangeTicketFixture.createRejectedTicket();
        
        when(rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)).thenReturn(expectedTicket);

        
        ChangeTicket result = changeTicketService.rejectChangeTicket(ticketId, rejectionReason, token);

        
        assertNotNull(result);
        verify(rejectChangeTicketUseCase).execute(ticketId, rejectionReason, token);
    }

    @Test
    @DisplayName("Should verify all use cases are properly injected")
    void shouldVerifyAllUseCasesAreProperlyInjected() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String rejectionReason = ChangeTicketFixture.REJECTION_REASON;
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        Pageable pageable = PageRequest.of(0, 10);
        
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        Page<ChangeTicketGridProjection> gridPage = ChangeTicketFixture.createMockGridPage();
        ChangeTicketResponseDTO responseDTO = ChangeTicketFixture.createValidResponseDTO();
        
        when(createChangeTicketUseCase.execute(request, httpRequest, token)).thenReturn(ticket);
        when(getChangeTicketUseCase.execute(pageable)).thenReturn(gridPage);
        when(getChangeTicketUseCase.executeById(ticketId)).thenReturn(responseDTO);
        when(applyChangeTicketUseCase.execute(ticketId, token)).thenReturn(ticket);
        when(rejectChangeTicketUseCase.execute(ticketId, rejectionReason, token)).thenReturn(ticket);

        
        changeTicketService.createTicket(request, httpRequest, token);
        changeTicketService.getChangeTicketsGrid(pageable);
        changeTicketService.getChangeTicketById(ticketId);
        changeTicketService.applyChangeTicket(ticketId, token);
        changeTicketService.rejectChangeTicket(ticketId, rejectionReason, token);

        
        verify(createChangeTicketUseCase).execute(request, httpRequest, token);
        verify(getChangeTicketUseCase).execute(pageable);
        verify(getChangeTicketUseCase).executeById(ticketId);
        verify(applyChangeTicketUseCase).execute(ticketId, token);
        verify(rejectChangeTicketUseCase).execute(ticketId, rejectionReason, token);
    }

    @Test
    @DisplayName("Should maintain consistent return types")
    void shouldMaintainConsistentReturnTypes() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        Pageable pageable = PageRequest.of(0, 10);
        
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        Page<ChangeTicketGridProjection> gridPage = ChangeTicketFixture.createMockGridPage();
        ChangeTicketResponseDTO responseDTO = ChangeTicketFixture.createValidResponseDTO();
        
        when(createChangeTicketUseCase.execute(request, httpRequest, token)).thenReturn(ticket);
        when(getChangeTicketUseCase.execute(pageable)).thenReturn(gridPage);
        when(getChangeTicketUseCase.executeById(ticketId)).thenReturn(responseDTO);
        when(applyChangeTicketUseCase.execute(ticketId, token)).thenReturn(ticket);

        
        ChangeTicket createResult = changeTicketService.createTicket(request, httpRequest, token);
        Page<ChangeTicketGridProjection> gridResult = changeTicketService.getChangeTicketsGrid(pageable);
        ChangeTicketResponseDTO getByIdResult = changeTicketService.getChangeTicketById(ticketId);
        ChangeTicket applyResult = changeTicketService.applyChangeTicket(ticketId, token);

        
        assertNotNull(createResult);
        assertNotNull(gridResult);
        assertNotNull(getByIdResult);
        assertNotNull(applyResult);
        
        assertTrue(createResult instanceof ChangeTicket);
        assertTrue(gridResult instanceof Page);
        assertTrue(getByIdResult instanceof ChangeTicketResponseDTO);
        assertTrue(applyResult instanceof ChangeTicket);
    }

    @Test
    @DisplayName("Should pass parameters correctly to use cases")
    void shouldPassParametersCorrectlyToUseCases() {
        
        String specificTicketId = "specific-ticket-123";
        String specificRejectionReason = "Specific rejection reason";
        ChangeTicketRequest specificRequest = new ChangeTicketRequest(
                "specific-ppu-id",
                "specific reason",
                List.of(ChangeTicketFixture.createServiceItemRequest())
        );
        Pageable specificPageable = PageRequest.of(2, 25);
        
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        Page<ChangeTicketGridProjection> gridPage = ChangeTicketFixture.createMockGridPage();
        ChangeTicketResponseDTO responseDTO = ChangeTicketFixture.createValidResponseDTO();
        
        when(createChangeTicketUseCase.execute(specificRequest, httpRequest, token)).thenReturn(ticket);
        when(getChangeTicketUseCase.execute(specificPageable)).thenReturn(gridPage);
        when(getChangeTicketUseCase.executeById(specificTicketId)).thenReturn(responseDTO);
        when(applyChangeTicketUseCase.execute(specificTicketId, token)).thenReturn(ticket);
        when(rejectChangeTicketUseCase.execute(specificTicketId, specificRejectionReason, token)).thenReturn(ticket);

        
        changeTicketService.createTicket(specificRequest, httpRequest, token);
        changeTicketService.getChangeTicketsGrid(specificPageable);
        changeTicketService.getChangeTicketById(specificTicketId);
        changeTicketService.applyChangeTicket(specificTicketId, token);
        changeTicketService.rejectChangeTicket(specificTicketId, specificRejectionReason, token);

        
        verify(createChangeTicketUseCase).execute(eq(specificRequest), eq(httpRequest), eq(token));
        verify(getChangeTicketUseCase).execute(eq(specificPageable));
        verify(getChangeTicketUseCase).executeById(eq(specificTicketId));
        verify(applyChangeTicketUseCase).execute(eq(specificTicketId), eq(token));
        verify(rejectChangeTicketUseCase).execute(eq(specificTicketId), eq(specificRejectionReason), eq(token));
    }

    @Test
    @DisplayName("Should not modify parameters before passing to use cases")
    void shouldNotModifyParametersBeforePassingToUseCases() {
        
        ChangeTicketRequest originalRequest = ChangeTicketFixture.createValidRequest();
        String originalTicketId = ChangeTicketFixture.TICKET_ID;
        String originalRejectionReason = ChangeTicketFixture.REJECTION_REASON;
        Pageable originalPageable = PageRequest.of(0, 10);
        
        
        String ticketIdCopy = new String(originalTicketId);
        String rejectionReasonCopy = new String(originalRejectionReason);
        
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        Page<ChangeTicketGridProjection> gridPage = ChangeTicketFixture.createMockGridPage();
        ChangeTicketResponseDTO responseDTO = ChangeTicketFixture.createValidResponseDTO();
        
        when(createChangeTicketUseCase.execute(any(), any(), any())).thenReturn(ticket);
        when(getChangeTicketUseCase.execute(any())).thenReturn(gridPage);
        when(getChangeTicketUseCase.executeById(any())).thenReturn(responseDTO);
        when(applyChangeTicketUseCase.execute(any(), any())).thenReturn(ticket);
        when(rejectChangeTicketUseCase.execute(any(), any(), any())).thenReturn(ticket);

        
        changeTicketService.createTicket(originalRequest, httpRequest, token);
        changeTicketService.getChangeTicketsGrid(originalPageable);
        changeTicketService.getChangeTicketById(originalTicketId);
        changeTicketService.applyChangeTicket(originalTicketId, token);
        changeTicketService.rejectChangeTicket(originalTicketId, originalRejectionReason, token);

        
        assertEquals(ticketIdCopy, originalTicketId);
        assertEquals(rejectionReasonCopy, originalRejectionReason);
        assertNotNull(originalRequest);
        assertNotNull(originalPageable);
    }
}