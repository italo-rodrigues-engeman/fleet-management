package com.indux.modules.ppu.domain.entities.rdo.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MioDivergence {
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
