package com.indux.modules.ppu.application.dtos.rdo.itens;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EmployeeDayType {
    @JsonProperty("PRIMEIRO DIA")
    FIRST_DAY,
    @JsonProperty("ROTINA")
    ROUTINE
}
