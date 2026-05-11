package com.indux.modules.ppu.application.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import lombok.*;

import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Builder @Getter @Setter
public class CalculatorRequest {
    @JsonProperty("horario") private ShiftSchedule schedule;
    @JsonProperty("horasExtras") private List<EmployeeOvertime> overtimes;

}
