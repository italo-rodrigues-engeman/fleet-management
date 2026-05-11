package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import com.indux.modules.ppu.infra.mapper.ticket.ChangeTicketResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetChangeTicketUseCaseTest {

    private ChangeTicketRepository changeTicketRepository;
    private ChangeTicketResponseMapper mapper;
    private GetChangeTicketUseCase getChangeTicketUseCase;

    @BeforeEach
    void setUp() {
        changeTicketRepository = mock(ChangeTicketRepository.class);
        mapper = mock(ChangeTicketResponseMapper.class);
        getChangeTicketUseCase = new GetChangeTicketUseCase(changeTicketRepository, mapper);
    }

    @Test
    @DisplayName("Should return paginated change tickets grid successfully")
    void shouldReturnPaginatedChangeTicketsGridSuccessfully() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChangeTicketGridProjection> expectedPage = ChangeTicketFixture.createMockGridPage();
        
        when(changeTicketRepository.findAllBy(pageable)).thenReturn(expectedPage);

        
        Page<ChangeTicketGridProjection> result = getChangeTicketUseCase.execute(pageable);

        
        assertNotNull(result);
        assertEquals(expectedPage.getContent().size(), result.getContent().size());
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        
        verify(changeTicketRepository).findAllBy(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no tickets exist")
    void shouldReturnEmptyPageWhenNoTicketsExist() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChangeTicketGridProjection> emptyPage = Page.empty(pageable);
        
        when(changeTicketRepository.findAllBy(pageable)).thenReturn(emptyPage);

        
        Page<ChangeTicketGridProjection> result = getChangeTicketUseCase.execute(pageable);

        
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(changeTicketRepository).findAllBy(pageable);
    }

    @Test
    @DisplayName("Should return paginated change tickets by PPU ID successfully")
    void shouldReturnPaginatedChangeTicketsByPPUIdSuccessfully() {
        
        String ppuId = ChangeTicketFixture.PPU_ID;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChangeTicketGridProjection> expectedPage = ChangeTicketFixture.createMockGridPage();
        
        when(changeTicketRepository.findByPpuId(ppuId, pageable)).thenReturn(expectedPage);

        
        Page<ChangeTicketGridProjection> result = getChangeTicketUseCase.executeByPpuId(ppuId, pageable);

        
        assertNotNull(result);
        assertEquals(expectedPage.getContent().size(), result.getContent().size());
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        
        verify(changeTicketRepository).findByPpuId(ppuId, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no tickets exist for PPU ID")
    void shouldReturnEmptyPageWhenNoTicketsExistForPPUId() {
        
        String ppuId = ChangeTicketFixture.PPU_ID;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChangeTicketGridProjection> emptyPage = Page.empty(pageable);
        
        when(changeTicketRepository.findByPpuId(ppuId, pageable)).thenReturn(emptyPage);

        
        Page<ChangeTicketGridProjection> result = getChangeTicketUseCase.executeByPpuId(ppuId, pageable);

        
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(changeTicketRepository).findByPpuId(ppuId, pageable);
    }

    @Test
    @DisplayName("Should return change ticket by ID successfully")
    void shouldReturnChangeTicketByIdSuccessfully() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        ChangeTicketResponseDTO expectedResponse = ChangeTicketFixture.createValidResponseDTO();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(mapper.toResponseDTO(ticket)).thenReturn(expectedResponse);

        
        ChangeTicketResponseDTO result = getChangeTicketUseCase.executeById(ticketId);

        
        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.ppuId(), result.ppuId());
        assertEquals(expectedResponse.status(), result.status());
        
        verify(changeTicketRepository).findById(ticketId);
        verify(mapper).toResponseDTO(ticket);
    }

    @Test
    @DisplayName("Should throw exception when ticket not found by ID")
    void shouldThrowExceptionWhenTicketNotFoundById() {
        
        String ticketId = "non-existent-ticket";
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.empty());

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> getChangeTicketUseCase.executeById(ticketId)
        );
        
        assertEquals("Ticket não encontrado: " + ticketId, exception.getMessage());
        verify(changeTicketRepository).findById(ticketId);
        verify(mapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Should return change ticket with applied status by ID")
    void shouldReturnChangeTicketWithAppliedStatusById() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket appliedTicket = ChangeTicketFixture.createAppliedTicket();
        ChangeTicketResponseDTO expectedResponse = ChangeTicketFixture.createValidResponseDTO();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(appliedTicket));
        when(mapper.toResponseDTO(appliedTicket)).thenReturn(expectedResponse);

        
        ChangeTicketResponseDTO result = getChangeTicketUseCase.executeById(ticketId);

        
        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        
        verify(changeTicketRepository).findById(ticketId);
        verify(mapper).toResponseDTO(appliedTicket);
    }

    @Test
    @DisplayName("Should return change ticket with rejected status by ID")
    void shouldReturnChangeTicketWithRejectedStatusById() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket rejectedTicket = ChangeTicketFixture.createRejectedTicket();
        ChangeTicketResponseDTO expectedResponse = ChangeTicketFixture.createValidResponseDTO();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(rejectedTicket));
        when(mapper.toResponseDTO(rejectedTicket)).thenReturn(expectedResponse);

        
        ChangeTicketResponseDTO result = getChangeTicketUseCase.executeById(ticketId);

        
        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        
        verify(changeTicketRepository).findById(ticketId);
        verify(mapper).toResponseDTO(rejectedTicket);
    }

    @Test
    @DisplayName("Should handle different page sizes correctly")
    void shouldHandleDifferentPageSizesCorrectly() {
        
        Pageable smallPageable = PageRequest.of(0, 5);
        Pageable largePageable = PageRequest.of(0, 50);
        Page<ChangeTicketGridProjection> smallPage = Page.empty(smallPageable);
        Page<ChangeTicketGridProjection> largePage = Page.empty(largePageable);
        
        when(changeTicketRepository.findAllBy(smallPageable)).thenReturn(smallPage);
        when(changeTicketRepository.findAllBy(largePageable)).thenReturn(largePage);

        
        Page<ChangeTicketGridProjection> smallResult = getChangeTicketUseCase.execute(smallPageable);
        Page<ChangeTicketGridProjection> largeResult = getChangeTicketUseCase.execute(largePageable);

        
        assertNotNull(smallResult);
        assertNotNull(largeResult);
        assertEquals(5, smallResult.getSize());
        assertEquals(50, largeResult.getSize());
        
        verify(changeTicketRepository).findAllBy(smallPageable);
        verify(changeTicketRepository).findAllBy(largePageable);
    }

    @Test
    @DisplayName("Should handle different page numbers correctly")
    void shouldHandleDifferentPageNumbersCorrectly() {
        
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);
        Page<ChangeTicketGridProjection> firstPageResult = Page.empty(firstPage);
        Page<ChangeTicketGridProjection> secondPageResult = Page.empty(secondPage);
        
        when(changeTicketRepository.findAllBy(firstPage)).thenReturn(firstPageResult);
        when(changeTicketRepository.findAllBy(secondPage)).thenReturn(secondPageResult);

        
        Page<ChangeTicketGridProjection> firstResult = getChangeTicketUseCase.execute(firstPage);
        Page<ChangeTicketGridProjection> secondResult = getChangeTicketUseCase.execute(secondPage);

        
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertEquals(0, firstResult.getNumber());
        assertEquals(1, secondResult.getNumber());
        
        verify(changeTicketRepository).findAllBy(firstPage);
        verify(changeTicketRepository).findAllBy(secondPage);
    }

    @Test
    @DisplayName("Should handle null pageable parameter gracefully")
    void shouldHandleNullPageableParameterGracefully() {
        
        Pageable nullPageable = null;
        
        when(changeTicketRepository.findAllBy(nullPageable)).thenThrow(new IllegalArgumentException("Pageable cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getChangeTicketUseCase.execute(nullPageable)
        );
        
        assertEquals("Pageable cannot be null", exception.getMessage());
        verify(changeTicketRepository).findAllBy(nullPageable);
    }

    @Test
    @DisplayName("Should handle null PPU ID parameter gracefully")
    void shouldHandleNullPPUIdParameterGracefully() {
        
        String nullPpuId = null;
        Pageable pageable = PageRequest.of(0, 10);
        
        when(changeTicketRepository.findByPpuId(nullPpuId, pageable)).thenThrow(new IllegalArgumentException("PPU ID cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getChangeTicketUseCase.executeByPpuId(nullPpuId, pageable)
        );
        
        assertEquals("PPU ID cannot be null", exception.getMessage());
        verify(changeTicketRepository).findByPpuId(nullPpuId, pageable);
    }

    @Test
    @DisplayName("Should handle mapper exceptions gracefully")
    void shouldHandleMapperExceptionsGracefully() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(mapper.toResponseDTO(ticket)).thenThrow(new RuntimeException("Mapping error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> getChangeTicketUseCase.executeById(ticketId)
        );
        
        assertEquals("Mapping error", exception.getMessage());
        verify(changeTicketRepository).findById(ticketId);
        verify(mapper).toResponseDTO(ticket);
    }

    @Test
    @DisplayName("Should verify repository is called with correct parameters")
    void shouldVerifyRepositoryIsCalledWithCorrectParameters() {
        
        String ticketId = ChangeTicketFixture.TICKET_ID;
        String ppuId = ChangeTicketFixture.PPU_ID;
        Pageable pageable = PageRequest.of(2, 20);
        
        ChangeTicket ticket = ChangeTicketFixture.createPendingTicket();
        Page<ChangeTicketGridProjection> gridPage = ChangeTicketFixture.createMockGridPage();
        ChangeTicketResponseDTO responseDTO = ChangeTicketFixture.createValidResponseDTO();
        
        when(changeTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(changeTicketRepository.findAllBy(pageable)).thenReturn(gridPage);
        when(changeTicketRepository.findByPpuId(ppuId, pageable)).thenReturn(gridPage);
        when(mapper.toResponseDTO(ticket)).thenReturn(responseDTO);

        
        getChangeTicketUseCase.executeById(ticketId);
        getChangeTicketUseCase.execute(pageable);
        getChangeTicketUseCase.executeByPpuId(ppuId, pageable);

        
        verify(changeTicketRepository).findById(ticketId);
        verify(changeTicketRepository).findAllBy(pageable);
        verify(changeTicketRepository).findByPpuId(ppuId, pageable);
        verify(mapper).toResponseDTO(ticket);
    }
}