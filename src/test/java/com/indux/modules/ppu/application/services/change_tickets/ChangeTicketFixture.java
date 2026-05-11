package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.application.services.fixtures.PPUFixture;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeTicketFixture {

    
    public static final String TICKET_ID = "ticket-123";
    public static final String PPU_ID = "ppu-456";
    public static final String USER_ID = "user-789";
    public static final String USER_NAME = "João Silva";
    public static final String REASON = "Ajuste de quantidade para atender demanda do projeto";
    public static final String REJECTION_REASON = "Quantidade solicitada excede o orçamento disponível";
    public static final String IP_ADDRESS = "192.168.1.100";
    public static final String SERVICE_ID = "TESTE";
    public static final String PLATFORM = "PRA-1";
    public static final Integer QUANTITY = 10;
    public static final Integer NEW_QUANTITY = 15;

    
    public static ChangeTicket.Actor createActor() {
        return ChangeTicket.Actor.builder()
                .userId(USER_ID)
                .name(USER_NAME)
                .build();
    }

    public static ChangeTicket.Source createSource() {
        return ChangeTicket.Source.builder()
                .service("kogni")
                .ip(IP_ADDRESS)
                .build();
    }

    public static ChangeTicket.ChangeItem createServiceChangeItem() {
        return ChangeTicket.ChangeItem.builder()
                .itemId(SERVICE_ID)
                .lineType(ChangeTicket.ChangeItem.LineType.SERVICE)
                .platform(PLATFORM)
                .quantity(QUANTITY)
                .build();
    }

    public static ChangeTicket.ChangeItem createEquipmentChangeItem() {
        return ChangeTicket.ChangeItem.builder()
                .itemId("ID-EQUIPAMENTO")
                .lineType(ChangeTicket.ChangeItem.LineType.EQUIPMENT)
                .platform(PLATFORM)
                .quantity(QUANTITY)
                .build();
    }

    public static ChangeTicket.ChangeItem createSteelCableChangeItem() {
        return ChangeTicket.ChangeItem.builder()
                .itemId("ID")
                .lineType(ChangeTicket.ChangeItem.LineType.STEEL_CABLE)
                .platform(null) 
                .quantity(QUANTITY)
                .build();
    }

    public static ChangeTicket.ChangeItem createAccessoryKitChangeItem() {
        return ChangeTicket.ChangeItem.builder()
                .itemId("kit-123")
                .lineType(ChangeTicket.ChangeItem.LineType.ACCESSORY_KIT)
                .platform(PLATFORM)
                .quantity(QUANTITY)
                .build();
    }

    public static ChangeTicket.HistoryLog createHistoryLog() {
        return ChangeTicket.HistoryLog.builder()
                .operation("update")
                .path("/services/" + SERVICE_ID + "/measurementForecasts/" + PLATFORM + "/total")
                .from(5)
                .to(QUANTITY)
                .build();
    }

    public static ChangeTicket createPendingTicket() {
        return ChangeTicket.builder()
                .id(TICKET_ID)
                .ppuId(PPU_ID)
                .reason(REASON)
                .fromVersion(0L)
                .toVersion(1L)
                .actor(createActor())
                .source(createSource())
                .item(List.of(createServiceChangeItem()))
                .diff(List.of(createHistoryLog()))
                .contract(Map.of("id", 1L, "name", "Contrato Teste"))
                .regionalId(1L)
                .regionalName("RJ")
                .apelido("PPU Teste")
                .status(ChangeTicket.TicketStatus.PENDING)
                .createdAt(Instant.now())
                .createdBy(USER_ID)
                .build();
    }

    public static ChangeTicket createAppliedTicket() {
        return createPendingTicket().toBuilder()
                .status(ChangeTicket.TicketStatus.APPLIED)
                .approvedAt(Instant.now())
                .approvedBy(USER_ID)
                .approverName(USER_NAME)
                .build();
    }

    public static ChangeTicket createRejectedTicket() {
        return createPendingTicket().toBuilder()
                .status(ChangeTicket.TicketStatus.REJECTED)
                .approvedAt(Instant.now())
                .approvedBy(USER_ID)
                .approverName(USER_NAME)
                .rejectionReason(REJECTION_REASON)
                .build();
    }

    
    public static ChangeTicketRequest.ChangeTicketItemRequest createServiceItemRequest() {
        return new ChangeTicketRequest.ChangeTicketItemRequest(
                SERVICE_ID,
                "SERVICE",
                PLATFORM,
                QUANTITY
        );
    }

    public static ChangeTicketRequest.ChangeTicketItemRequest createEquipmentItemRequest() {
        return new ChangeTicketRequest.ChangeTicketItemRequest(
                "ID-EQUIPAMENTO",
                "EQUIPMENT",
                PLATFORM,
                QUANTITY
        );
    }

    public static ChangeTicketRequest.ChangeTicketItemRequest createSteelCableItemRequest() {
        return new ChangeTicketRequest.ChangeTicketItemRequest(
                "ID",
                "STEEL_CABLE",
                null,
                QUANTITY
        );
    }

    public static ChangeTicketRequest.ChangeTicketItemRequest createAccessoryKitItemRequest() {
        return new ChangeTicketRequest.ChangeTicketItemRequest(
                "kit-123",
                "ACCESSORY_KIT",
                PLATFORM,
                QUANTITY
        );
    }

    public static ChangeTicketRequest createValidRequest() {
        return new ChangeTicketRequest(
                PPU_ID,
                REASON,
                List.of(createServiceItemRequest())
        );
    }

    public static ChangeTicketRequest createMultiItemRequest() {
        return new ChangeTicketRequest(
                PPU_ID,
                REASON,
                List.of(
                        createServiceItemRequest(),
                        createEquipmentItemRequest(),
                        createSteelCableItemRequest(),
                        createAccessoryKitItemRequest()
                )
        );
    }

    public static ChangeTicketRequest createRequestWithoutItems() {
        return new ChangeTicketRequest(PPU_ID, REASON, null);
    }

    
    public static JwtAuthenticationToken createMockJwtToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("name")).thenReturn(USER_NAME);
        
        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(token.getName()).thenReturn(USER_ID);
        when(token.getToken()).thenReturn(jwt);
        
        return token;
    }

    public static HttpServletRequest createMockHttpRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);
        return request;
    }

    
    public static ChangeTicketResponseDTO createValidResponseDTO() {
        ChangeTicketResponseDTO responseDTO = mock(ChangeTicketResponseDTO.class);
        when(responseDTO.id()).thenReturn(TICKET_ID);
        when(responseDTO.ppuId()).thenReturn(PPU_ID);
        when(responseDTO.status()).thenReturn(ChangeTicket.TicketStatus.PENDING.name());
        return responseDTO;
    }

    
    public static ChangeTicketGridProjection createMockGridProjection() {
        ChangeTicketGridProjection projection = mock(ChangeTicketGridProjection.class);
        when(projection.getId()).thenReturn(TICKET_ID);
        when(projection.getPpuId()).thenReturn(PPU_ID);
        when(projection.getCreatedAt()).thenReturn(Instant.now());
        when(projection.getReason()).thenReturn(REASON);
        when(projection.getFromVersion()).thenReturn(0L);
        when(projection.getToVersion()).thenReturn(1L);
        when(projection.getApelido()).thenReturn("PPU Teste");
        when(projection.getRegionalName()).thenReturn("RJ");
        when(projection.getStatus()).thenReturn("PENDING");
        
        ChangeTicketGridProjection.Actor actor = mock(ChangeTicketGridProjection.Actor.class);
        when(actor.getUserId()).thenReturn(USER_ID);
        when(actor.getName()).thenReturn(USER_NAME);
        when(projection.getActor()).thenReturn(actor);
        
        return projection;
    }

    public static Page<ChangeTicketGridProjection> createMockGridPage() {
        return new PageImpl<>(
                List.of(createMockGridProjection()),
                PageRequest.of(0, 10),
                1
        );
    }

    
    public static PPUEntity createPPUWithVersion(Long version) {
        PPUEntity ppu = PPUFixture.fakePPUEntityFunction();
        ppu.setVersion(version);
        return ppu;
    }

    
    public static ChangeTicketRequest.ChangeTicketItemRequest createInvalidLineTypeRequest() {
        return new ChangeTicketRequest.ChangeTicketItemRequest(
                SERVICE_ID,
                "INVALID_TYPE",
                PLATFORM,
                QUANTITY
        );
    }

    
    public static ChangeTicketRequest.ChangeTicketItemRequest createInvalidItemRequest() {
        return new ChangeTicketRequest.ChangeTicketItemRequest(
                null, 
                "SERVICE",
                PLATFORM,
                QUANTITY
        );
    }

    
    public static ChangeTicket.ChangeItem createChangeItemWithQuantity(Integer quantity) {
        return ChangeTicket.ChangeItem.builder()
                .itemId(SERVICE_ID)
                .lineType(ChangeTicket.ChangeItem.LineType.SERVICE)
                .platform(PLATFORM)
                .quantity(quantity)
                .build();
    }

    
    public static ChangeTicket createTicketWithStatus(ChangeTicket.TicketStatus status) {
        return createPendingTicket().toBuilder()
                .status(status)
                .build();
    }
}