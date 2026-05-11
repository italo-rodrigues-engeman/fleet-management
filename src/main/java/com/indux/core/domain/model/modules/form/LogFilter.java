package com.indux.core.domain.model.modules.form;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class LogFilter {
    private String occurrenceId;
    private UUID userId;
    private Integer step;
    private String stepName;
    private Date createdAtFrom;
    private Date createdAtTo;
}