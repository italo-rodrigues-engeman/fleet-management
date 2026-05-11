package com.indux.modules.ppu.domain.strategy.time;

import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.impl.OvertimeAnotherLineStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class OvertimeAnotherLineStrategyTest {
    @InjectMocks
    private OvertimeAnotherLineStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return RDOServices with overtime in value measured")
    void calculate() {
        var servicesLineFixtures = new ArrayList<>(List.of(Fixtures.serviceLine, Fixtures.serviceLine2, Fixtures.serviceLine3));
        var servicesFromRDOFixtures = new ArrayList<>(List.of(Fixtures.service, Fixtures.service2, Fixtures.service3));

        var result = strategy.produceVirtuals(servicesFromRDOFixtures, servicesLineFixtures);
        var overtime = result.stream().filter(e -> e.getServiceID().equals("referencia_hora_extra")).findFirst().orElseThrow();

        assertEquals(Duration.ofHours(8), overtime.getOvertimeHourTotais());
        assertEquals(8.0, overtime.getValueMeasured());
        assertEquals(1, result.size());

        servicesLineFixtures.addAll(List.of(Fixtures.serviceLine4, Fixtures.serviceLine5));
        servicesFromRDOFixtures.add(Fixtures.service4);

        result = strategy.produceVirtuals(servicesFromRDOFixtures, servicesLineFixtures);
        var overtime2 = result.stream().filter(e -> e.getServiceID().equals("referencia_hora_extra2")).findFirst().orElseThrow();
        assertEquals(2, result.size());
        assertEquals(8.0, overtime.getValueMeasured());
        assertEquals(2.5, overtime2.getValueMeasured());
    }

    @Test
    @DisplayName("Should returns empty when services or ppuServices are null/empty")
    void calculate_shouldReturnEmpty_forNullOrEmptyInputs() {
        assertTrue(strategy.produceVirtuals(null, Fixtures.ppuBase()).isEmpty());
        assertTrue(strategy.produceVirtuals(List.of(), Fixtures.ppuBase()).isEmpty());
        assertTrue(strategy.produceVirtuals(Fixtures.rdoBase(), null).isEmpty());
        assertTrue(strategy.produceVirtuals(Fixtures.rdoBase(), List.of()).isEmpty());
    }

    @Test
    @DisplayName("Should ignores RDO services that are not found in PPU (no NPE)")
    void calculate_shouldIgnoreServicesNotInPPU() {
        var servicesLineFixtures = new ArrayList<>(List.of(Fixtures.serviceLine, Fixtures.serviceLine2, Fixtures.serviceLine3));

        var rdo = new ArrayList<>(Fixtures.rdoBase());
        rdo.add(RDOServiceEntity.builder()
                .serviceID("nao_existe_no_ppu")
                .overtimeHourTotais(Duration.ofHours(10))
                .valueMeasured(1.0)
                .build());

        var result = strategy.produceVirtuals(rdo, servicesLineFixtures);

        var overtime = findById(result, "referencia_hora_extra");
        assertEquals(1, result.size());
        assertEquals(8.0, overtime.getValueMeasured());
    }

    private static RDOServiceEntity findById(List<RDOServiceEntity> list, String id) {
        return list.stream()
                .filter(e -> Objects.equals(e.getServiceID(), id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ServiceID não encontrado: " + id + " em " +
                        list.stream().map(RDOServiceEntity::getServiceID).toList()));
    }


    @Test
    void getOvertimeChildren() {
    }

    @Test
    void getName() {
    }


    static class Fixtures {
        public static RDOServiceEntity service = RDOServiceEntity.builder().serviceID("referencia1").valueMeasured(1.0).overtimeHourTotais(Duration.ofHours(2)).build();

        public static RDOServiceEntity service3 = RDOServiceEntity.builder().serviceID("referencia1").overtimeHourTotais(Duration.ofHours(3)).valueMeasured(1.0).build();

        public static RDOServiceEntity service2 = RDOServiceEntity.builder().serviceID("referencia2").overtimeHourTotais(Duration.ofHours(3)).valueMeasured(1.0).build();

        public static ServiceLine serviceLine = ServiceLine.builder().id("referencia1").isOvertimeService(false).overtimeService("referencia_hora_extra").timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE).build();

        public static ServiceLine serviceLine2 = ServiceLine.builder().id("referencia2").isOvertimeService(false).parentId("referencia1").timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE).overtimeService("referencia_hora_extra").build();

        public static ServiceLine serviceLine3 = ServiceLine.builder().id("referencia_hora_extra").timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE).parentId("referencia1").isOvertimeService(true).build();
        static List<RDOServiceEntity> rdoBase() {
            return new ArrayList<>(List.of(service, service2, service3));
        }
        static List<ServiceLine> ppuBase() {
            return new ArrayList<>(List.of(serviceLine, serviceLine2, serviceLine3));
        }
        /// ///////////////////////
        public static RDOServiceEntity service4 = RDOServiceEntity.builder().serviceID("referencia3").valueMeasured(1.0).overtimeHourTotais(Duration.ofHours(2).plusMinutes(30)).build();
        public static ServiceLine serviceLine4 = ServiceLine.builder().id("referencia3").timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE).isOvertimeService(false).parentId("referencia1").overtimeService("referencia_hora_extra2").name("Serviço de Hora Extra").build();
        public static ServiceLine serviceLine5 = ServiceLine.builder().id("referencia_hora_extra2").timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE).isOvertimeService(true).parentId("referencia3").build();
    }
}