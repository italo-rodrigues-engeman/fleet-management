package com.indux.modules.ppu.domain.entities.ppu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditableConfig {
    private String columnName;
    private Integer columnIndex;
}
