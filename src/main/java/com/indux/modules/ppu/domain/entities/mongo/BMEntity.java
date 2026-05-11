package com.indux.modules.ppu.domain.entities.mongo;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.response.bm.BMMonthlyItem;
import com.indux.modules.ppu.application.dtos.response.bm.BMPlatformReport;
import com.indux.modules.ppu.domain.entities.bm.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "ppu_bm")
public class BMEntity {
    @Id
    private String id;
    @CreatedBy
    private String createdBy;
    private String approvedBy;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    private Instant approvedAt;
    private DocumentStatus status;

    // -- General Information --//
    @DBRef
    private PPUEntity ppu;
    private Long projectId;
    private DateRange period;
    private Map<String, Object> contract;
    private List<String> rdosClosed;
    private String justification;
    @Builder.Default
    private BigDecimal valueClosed = BigDecimal.ZERO;

    public String getPpuId() {
        return ppu != null ? ppu.getId() : null;
    }

    // -- Calc information -- //
    private List<BMTimelineGeneral> timelines = new ArrayList<>();
    private List<BMDetailsGeneral> details = new ArrayList<>();
    private List<BMMonthlyItem> resumeMonthly = new ArrayList<>();
    private List<BMPlatformReport> resumePlatforms = new ArrayList<>();

    // --- Auditorias --- //
    private Boolean auditSAMCChecked = false;
    @Builder.Default
    private List<BMSamcLog> auditSAMCLog = new ArrayList<>();
    private Boolean auditRMChecked = false;
    @Builder.Default
    private List<RMLog> auditRMLog = new ArrayList<>();
    private Boolean auditMIOChecked = false;
    @Builder.Default
    private List<BMMioLog> auditMIOLog = new ArrayList<>();

    /**
     * Justificativas para divergências MIO.
     * Separado do {@code auditMIOLog} para não ser sobrescrito em re-auditorias.
     */
    @Builder.Default
    private List<MioDivergenceJustification> mioJustifications = new ArrayList<>();

    public boolean isFinished() {
        return status == DocumentStatus.APROVADO;
    }

}
