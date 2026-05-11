package com.indux.modules.ppu.application.dtos.rdo.itens;

import java.time.LocalTime;

public record EmployeeOvertime(
        LocalTime initial,
        LocalTime end
) {
}
