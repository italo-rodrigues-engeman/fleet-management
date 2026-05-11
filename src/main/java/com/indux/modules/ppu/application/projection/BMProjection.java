package com.indux.modules.ppu.application.projection;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.bm.RMLog;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface BMProjection {
     String getId();
     String getCreatedBy();
     String getApprovedBy();
     Instant getCreatedAt();
     Instant getUpdatedAt();
     Instant getApprovedAt();
     PPUEntity getPpu();
     Long getProjectId();
     DateRange getPeriod();
     DocumentStatus getStatus();
     List<RMLog> getAuditRMLog();
     BigDecimal getValueClosed();

}
