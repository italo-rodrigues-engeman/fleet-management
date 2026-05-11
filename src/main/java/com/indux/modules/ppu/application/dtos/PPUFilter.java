package com.indux.modules.ppu.application.dtos;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PPUFilter {
    private Long branch;
    private Integer contractId;
    private String platform;
    private DocumentStatus status;
    private String createdBy;
}