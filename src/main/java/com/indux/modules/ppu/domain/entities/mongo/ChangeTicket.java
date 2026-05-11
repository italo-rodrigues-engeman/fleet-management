package com.indux.modules.ppu.domain.entities.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
@Document("ppu_tickets")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true) @Getter @Setter
public class ChangeTicket {
    @Id
    private String id;
    private String ppuId;
    @CreatedDate
    private Instant createdAt;
    @CreatedBy
    private String createdBy;
    private List<HistoryLog> diff;
    private Long fromVersion;
    private Long toVersion;
    private String reason;
    private Actor actor;
    private Source source;
    private List<ChangeItem> item;
    
    private Map<String, Object> contract;
    private Long regionalId;
    private String regionalName;
    private String apelido;
    
    @Builder.Default
    private TicketStatus status = TicketStatus.PENDING;
    private Instant approvedAt;
    private String approvedBy;
    private String approverName;
    private String rejectionReason;
    
    public enum TicketStatus {
        @JsonProperty("PENDENTE") PENDING,     
        @JsonProperty("REJEITADO") REJECTED,    
        @JsonProperty("APLICADO") APPLIED      
    }

    @AllArgsConstructor @NoArgsConstructor @Getter
    @Setter @Builder
    public static class Actor {
        private String userId;
        private String name;
    }

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    @Builder
    public static class Source {
        private String service;
        private String ip;
    }
    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    @Builder
    public static class ChangeItem{
        private String itemId;
        private LineType lineType;
        private String platform;
        private Integer quantity;


        public enum LineType {
            @JsonProperty("SERVICO") SERVICE,
            @JsonProperty("EQUIPAMENTO") EQUIPMENT, 
            @JsonProperty("CABO_ACO") STEEL_CABLE,
            @JsonProperty("KIT_ACESSORIO") ACCESSORY_KIT;
        }
    }

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter @Builder
    public static class HistoryLog {
        private String operation;
        private String path;
        private Object from;
        private Object to;
    }
}
