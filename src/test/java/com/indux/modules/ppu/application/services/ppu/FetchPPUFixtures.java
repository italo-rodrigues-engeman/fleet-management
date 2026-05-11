package com.indux.modules.ppu.application.services.ppu;

import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FetchPPUFixtures {

    public static BoardedEmployee fakeBoardedEmployee(){
        return BoardedEmployee.builder()
                .registration("matricula-123")
                .name("Colaborador1")
                .position("Cargo_ID")
                .positionName("Cargo_Nome")
                .platform("P-01")
                .boardingForecast(LocalDate.of(2025, 9, 1))
                .boarding(LocalDate.of(2025, 9, 1))
                .landingForecast(LocalDate.of(2025, 9, 14))
                .sispat("SISPAT123")
                .contract(Map.of())
                .status("Embarque")
                .build();
    }

    public static BoardedEmployee fakeBoardedAvailableEmployee() {
        return BoardedEmployee.builder()
                .availableEndDate(LocalDate.now().minusDays(5))
                .build();
    }

    public static BoardedEmployee fakeBoardedEmployeeWithDayOff(){
        return
                BoardedEmployee.builder()
                        .registration("matricula-124")
                        .name("Colaborador2")
                        .position("Cargo_ID")
                        .positionName("Cargo_Nome")
                        .platform("P-01")
                        .boardingForecast(LocalDate.of(2025, 9, 1))
                        .boarding(LocalDate.of(2025, 9, 1))
                        .landingForecast(LocalDate.of(2025, 9, 14))
                        .sispat("SISPAT124")
                        .contract(Map.of())
                        .status("Folga")
                        .build();
    }

    public static BoardedEmployee fakeBoardedEmployeeP02(){
        return
                BoardedEmployee.builder()
                        .registration("matricula-125")
                        .name("Colaborador3")
                        .position("Cargo_ID")
                        .positionName("Cargo_Nome")
                        .platform("P-02")
                        .boardingForecast(LocalDate.of(2025, 9, 1))
                        .boarding(LocalDate.of(2025, 9, 1))
                        .landingForecast(LocalDate.of(2025, 9, 14))
                        .sispat("SISPAT125")
                        .contract(Map.of("id", 123))
                        .status("Embarque")
                        .filial_HCM("123")
                        .build();
    }

    public static BoardedEmployee fakeBoardedEmployeeP02DayOff() {
        return BoardedEmployee.builder()
                .registration("matricula-126")
                .name("Colaborador4")
                .position("Cargo_ID")
                .positionName("Cargo_Nome")
                .platform("P-02")
                .boardingForecast(LocalDate.of(2025, 9, 1))
                .boarding(LocalDate.of(2025, 9, 1))
                .landingForecast(LocalDate.of(2025, 9, 14))
                .sispat("SISPAT126")
                .contract(Map.of())
                .status("Folga")
                .build();
    }
}
