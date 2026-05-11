package com.indux.modules.ppu.domain.entities.bm;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
@Setter
public class RMLog {
    String id;
    RDOLoggerUser user;
    AttachmentEntity attachment;
    BigDecimal value;
    Instant upload_At;
}
