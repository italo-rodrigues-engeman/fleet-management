package com.indux.modules.ppu.application.dtos.response.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditMIOResponse {
    String idRDO;
    String platform;
    String statusMIO;
    LocalDate date;
    String statusRDO;
    String registration;
    String name;
    boolean justified;
    String justification;
}
