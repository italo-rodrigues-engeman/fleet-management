package com.indux.modules.alpar.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "alepar_audit_log")
public class AlparAuditLog {

    @Id
    private String id;

    private Instant timestamp;
    private String path;
    private String method;
    private int httpStatus;
    private long responseTimeMs;
    private String clientId;
    private String ip;
    private String userAgent;
    private String correlationId;
    private String result;
}
