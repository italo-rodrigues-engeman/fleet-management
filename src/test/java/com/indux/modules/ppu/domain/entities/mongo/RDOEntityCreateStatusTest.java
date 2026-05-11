package com.indux.modules.ppu.domain.entities.mongo;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.modules.ppu.application.dtos.item.ServiceItemDTO;
import com.indux.modules.ppu.application.dtos.rdo.RDORecord;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RDOEntityCreateStatusTest {

    @Test
    void fromDTOCreate_shouldStartApprovedWhenPpuHasNoSupervisorOnBoard() {
        var ppu = PPUEntity.builder()
                .id("ppu-1")
                .contract(Map.of("id", 1))
                .regionalNome("Regional")
                .regionalId(1L)
                .projectId(10L)
                .hasSupervisorOnBoard(false)
                .build();
        var employee = new EmployeeDTO();
        employee.setMatricula("123");
        employee.setName("User");

        var entity = RDOEntity.fromDTOCreate(baseRecord(), 1L, employee, ppu, "Cliente");

        assertEquals(RDOStatusOP.APPROVED, entity.getStatusOP());
        assertEquals(RDOStatusDP.PENDING, entity.getStatusDP());
    }

    @Test
    void fromDTOCreate_shouldStartPendingWhenPpuHasSupervisorOnBoard() {
        var ppu = PPUEntity.builder()
                .id("ppu-1")
                .contract(Map.of("id", 1))
                .regionalNome("Regional")
                .regionalId(1L)
                .projectId(10L)
                .hasSupervisorOnBoard(true)
                .build();
        var employee = new EmployeeDTO();
        employee.setMatricula("123");
        employee.setName("User");

        var entity = RDOEntity.fromDTOCreate(baseRecord(), 1L, employee, ppu, "Cliente");

        assertEquals(RDOStatusOP.PENDING, entity.getStatusOP());
        assertEquals(RDOStatusDP.PENDING, entity.getStatusDP());
    }

    private RDORecord baseRecord() {
        return new RDORecord(
                null,
                null,
                "PLAT-1",
                "ppu-1",
                LocalDate.now().minusDays(1),
                List.<ServiceItemDTO>of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                List.of(),
                "APP",
                null,
                null,
                null,
                10L,
                List.of(),
                false
        );
    }
}
