package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.ticket.ChangeTicketMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateChangeTicketUseCaseTest {

    private ChangeTicketRepository changeTicketRepository;
    private PPURepository ppuRepository;
    private ChangeTicketMapper mapper;
    private CreateChangeTicketUseCase createChangeTicketUseCase;
    private JwtAuthenticationToken token;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        changeTicketRepository = mock(ChangeTicketRepository.class);
        ppuRepository = mock(PPURepository.class);
        mapper = mock(ChangeTicketMapper.class);
        createChangeTicketUseCase = new CreateChangeTicketUseCase(changeTicketRepository, ppuRepository, mapper);
        token = ChangeTicketFixture.createMockJwtToken();
        httpRequest = ChangeTicketFixture.createMockHttpRequest();
    }

    @Test
    @DisplayName("Should create change ticket successfully with service items")
    void shouldCreateChangeTicketSuccessfullyWithServiceItems() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenReturn(expectedTicket);

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        assertEquals(expectedTicket.getId(), result.getId());
        assertEquals(ChangeTicket.TicketStatus.PENDING, result.getStatus());
        
        verify(ppuRepository).findById(request.ppuId());
        verify(changeTicketRepository).save(any(ChangeTicket.class));
        verify(mapper).toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class));
    }

    @Test
    @DisplayName("Should create change ticket with multiple item types")
    void shouldCreateChangeTicketWithMultipleItemTypes() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createMultiItemRequest();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(2L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenReturn(expectedTicket);

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        assertEquals(expectedTicket.getId(), result.getId());
        
        
        verify(mapper, times(4)).toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class));
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should create change ticket with null version PPU")
    void shouldCreateChangeTicketWithNullVersionPPU() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(null);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> {
            ChangeTicket ticket = invocation.getArgument(0);
            assertEquals(0L, ticket.getFromVersion());
            assertEquals(1L, ticket.getToVersion());
            return expectedTicket;
        });

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should create change ticket with null items list")
    void shouldCreateChangeTicketWithNullItemsList() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createRequestWithoutItems();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(1L);
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> {
            ChangeTicket ticket = invocation.getArgument(0);
            assertTrue(ticket.getItem().isEmpty());
            assertTrue(ticket.getDiff().isEmpty());
            return expectedTicket;
        });

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
        verify(mapper, never()).toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class));
    }

    @Test
    @DisplayName("Should create change ticket with proper actor information")
    void shouldCreateChangeTicketWithProperActorInformation() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> {
            ChangeTicket ticket = invocation.getArgument(0);
            assertEquals(ChangeTicketFixture.USER_ID, ticket.getActor().getUserId());
            assertEquals(ChangeTicketFixture.USER_NAME, ticket.getActor().getName());
            return ticket;
        });

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should create change ticket with proper source information")
    void shouldCreateChangeTicketWithProperSourceInformation() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> {
            ChangeTicket ticket = invocation.getArgument(0);
            assertEquals("kogni", ticket.getSource().getService());
            assertEquals(ChangeTicketFixture.IP_ADDRESS, ticket.getSource().getIp());
            return ticket;
        });

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should create change ticket with proper PPU context information")
    void shouldCreateChangeTicketWithProperPPUContextInformation() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenAnswer(invocation -> {
            ChangeTicket ticket = invocation.getArgument(0);
            assertEquals(ppu.getContract(), ticket.getContract());
            assertEquals(ppu.getRegionalId(), ticket.getRegionalId());
            assertEquals(ppu.getRegionalNome(), ticket.getRegionalName());
            assertEquals(ppu.getNickname(), ticket.getApelido());
            return ticket;
        });

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should throw exception when PPU not found")
    void shouldThrowExceptionWhenPPUNotFound() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.empty());

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> createChangeTicketUseCase.execute(request, httpRequest, token)
        );
        
        assertEquals("PPU não encontrada", exception.getMessage());
        verify(changeTicketRepository, never()).save(any());
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Should throw exception when service not found in PPU")
    void shouldThrowExceptionWhenServiceNotFoundInPPU() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        request.items().get(0).itemId(); 
        ChangeTicketRequest invalidRequest = new ChangeTicketRequest(
                request.ppuId(),
                request.reason(),
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        "non-existent-service",
                        "SERVICE",
                        ChangeTicketFixture.PLATFORM,
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(invalidRequest.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> createChangeTicketUseCase.execute(invalidRequest, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Serviço não encontrado na PPU"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when platform not found in service")
    void shouldThrowExceptionWhenPlatformNotFoundInService() {
        
        ChangeTicketRequest request = ChangeTicketFixture.createValidRequest();
        ChangeTicketRequest invalidRequest = new ChangeTicketRequest(
                request.ppuId(),
                request.reason(),
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        ChangeTicketFixture.SERVICE_ID,
                        "SERVICE",
                        "NON-EXISTENT-PLATFORM",
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(invalidRequest.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> createChangeTicketUseCase.execute(invalidRequest, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Plataforma não encontrada"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when equipment not found in PPU")
    void shouldThrowExceptionWhenEquipmentNotFoundInPPU() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        "non-existent-equipment",
                        "EQUIPMENT",
                        ChangeTicketFixture.PLATFORM,
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createEquipmentChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> createChangeTicketUseCase.execute(request, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Equipamento não encontrado"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when steel cable not found in PPU")
    void shouldThrowExceptionWhenSteelCableNotFoundInPPU() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        "non-existent-steel-cable",
                        "STEEL_CABLE",
                        null,
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createSteelCableChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> createChangeTicketUseCase.execute(request, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Cabo de aço não encontrado"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when accessory kit not found in PPU")
    void shouldThrowExceptionWhenAccessoryKitNotFoundInPPU() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        "non-existent-kit",
                        "ACCESSORY_KIT",
                        ChangeTicketFixture.PLATFORM,
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createAccessoryKitChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> createChangeTicketUseCase.execute(request, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Kit de acessório não encontrado"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for unsupported line type")
    void shouldThrowExceptionForUnsupportedLineType() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(ChangeTicketFixture.createInvalidLineTypeRequest())
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createChangeTicketUseCase.execute(request, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Tipo de linha não suportado"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for null line type")
    void shouldThrowExceptionForNullLineType() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        ChangeTicketFixture.SERVICE_ID,
                        null,
                        ChangeTicketFixture.PLATFORM,
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createServiceChangeItem();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createChangeTicketUseCase.execute(request, httpRequest, token)
        );
        
        assertTrue(exception.getMessage().contains("Tipo de linha não informado"));
        verify(changeTicketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle equipment with null platform correctly")
    void shouldHandleEquipmentWithNullPlatformCorrectly() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        "ID-EQUIPAMENTO",
                        "EQUIPMENT",
                        null, 
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createEquipmentChangeItem();
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenReturn(expectedTicket);

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }

    @Test
    @DisplayName("Should handle accessory kit with null platform correctly")
    void shouldHandleAccessoryKitWithNullPlatformCorrectly() {
        
        ChangeTicketRequest request = new ChangeTicketRequest(
                ChangeTicketFixture.PPU_ID,
                ChangeTicketFixture.REASON,
                List.of(new ChangeTicketRequest.ChangeTicketItemRequest(
                        "kit-123",
                        "ACCESSORY_KIT",
                        null, 
                        ChangeTicketFixture.QUANTITY
                ))
        );
        
        PPUEntity ppu = ChangeTicketFixture.createPPUWithVersion(0L);
        ChangeTicket.ChangeItem mappedItem = ChangeTicketFixture.createAccessoryKitChangeItem();
        ChangeTicket expectedTicket = ChangeTicketFixture.createPendingTicket();

        when(ppuRepository.findById(request.ppuId())).thenReturn(Optional.of(ppu));
        when(mapper.toEntity(any(ChangeTicketRequest.ChangeTicketItemRequest.class))).thenReturn(mappedItem);
        when(changeTicketRepository.save(any(ChangeTicket.class))).thenReturn(expectedTicket);

        
        ChangeTicket result = createChangeTicketUseCase.execute(request, httpRequest, token);

        
        assertNotNull(result);
        verify(changeTicketRepository).save(any(ChangeTicket.class));
    }
}