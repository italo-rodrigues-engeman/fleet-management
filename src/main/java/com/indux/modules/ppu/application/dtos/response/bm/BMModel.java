package com.indux.modules.ppu.application.dtos.response.bm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.bm.*;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BMModel(
        String id,
        @JsonProperty("criadoPor") String createdBy,
        @JsonProperty("aprovadoPor") String approvedBy,
        @JsonProperty("criadoEm") Instant createdAt,
        @JsonProperty("atualizadoEm") Instant updatedAt,
        @JsonProperty("aprovadoEm") Instant approvedAt,
        DocumentStatus status,
        PPUEntity ppu,
        @JsonProperty("projeto") Long projectId,
        @JsonProperty("periodo") DateRange period,
        @JsonProperty("rdos") List<String> rdosClosed,
        @JsonProperty("justificativa") String justification,
        @JsonProperty("valorFechado") BigDecimal closedValue,
        @JsonProperty("linhaTempo") List<BMTimelineGeneral> timelines,
        @JsonProperty("detalhes") List<BMDetailsGeneral> details,
        @JsonProperty("resumoMensal") List<BMMonthlyItem> resumeMonthly,
        @JsonProperty("resumoPlataformas") List<BMPlatformReport> resumePlatforms,

        @JsonProperty("auditoriaSamcChecado") Boolean auditSAMCChecked,
        @JsonProperty("auditoriaRMChecado") Boolean auditRMChecked,
        @JsonProperty("auditoriaMIOChecado") Boolean auditMIOChecked,
        @JsonProperty("auditoriaSamcLog") List<BMSamcLog> auditSAMCLog,
        @JsonProperty("auditoriaRMLog") List<RMLog> auditRMLog,
        @JsonProperty("auditoriaMioLog") List<BMMioLog> auditMIOLog,
        @JsonProperty("justificativasMio") List<MioDivergenceJustification> mioJustifications) {
    public String ppuId() {
        return ppu != null ? ppu.getId() : null;
    }
}
