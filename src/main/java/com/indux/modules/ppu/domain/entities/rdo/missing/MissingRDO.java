package com.indux.modules.ppu.domain.entities.rdo.missing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class MissingRDO {
    private String rdo;
    private LocalDate date;
    private String regional;
    private String platform;
    private String contract;
    private Long contractId;
}
