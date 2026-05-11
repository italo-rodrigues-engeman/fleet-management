package com.indux.modules.ppu.domain.strategy.time;

import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.ppu.ServiceType;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.strategy.time.impl.MoveOutStrategy;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveOutStrategyTest {
    
    private MoveOutStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MoveOutStrategy();
    }

    @Test
    @DisplayName("Should not move service when overtimes list contains only empty objects")
    void shouldNotMoveWhenOvertimesAreEmpty() {
        var parent = ServiceLine.builder()
                .id("parent-id")
                .name("Parent Service 12h")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .build();

        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service 24h")
                .genericNumber("1.1.1.2.3")
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("parent-id")
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service 24h")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of(new EmployeeOvertime(null, null)))
                .overtimeHourTotais(null)
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(parent, child);

        strategy.mutate(working, ppuServices);

        assertEquals("child-id", service.getServiceID());
        assertEquals("1.1.1.2.3", service.getServiceNumber());
        assertEquals("Child Service 24h", service.getServiceName());
    }

    @Test
    @DisplayName("Should move service when overtimes has valid initial and end times")
    void shouldMoveWhenOvertimesHaveValidTimes() {
        var parent = ServiceLine.builder()
                .id("parent-id")
                .name("Parent Service 12h")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .build();

        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service 24h")
                .genericNumber("1.1.1.2.3")
                .timeStrategy(TimeStrategyType.MOVE_OUT)
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("parent-id")
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service 24h")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of(new EmployeeOvertime(LocalTime.of(18, 0), LocalTime.of(19, 0))))
                .overtimeHourTotais(null)
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(parent, child);

        strategy.mutate(working, ppuServices);

        assertEquals("parent-id", service.getServiceID());
        assertEquals("1.1.1.2.1", service.getServiceNumber());
        assertEquals("Parent Service 12h", service.getServiceName());
    }

    @Test
    @DisplayName("Should move service when overtimeHourTotais is not zero")
    void shouldMoveWhenOvertimeHourTotaisIsNotZero() {
        var parent = ServiceLine.builder()
                .id("parent-id")
                .name("Parent Service 12h")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .build();

        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service 24h")
                .genericNumber("1.1.1.2.3")
                .timeStrategy(TimeStrategyType.MOVE_OUT)
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("parent-id")
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service 24h")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of())
                .overtimeHourTotais(Duration.ofMinutes(30))
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(parent, child);

        strategy.mutate(working, ppuServices);

        assertEquals("parent-id", service.getServiceID());
        assertEquals("1.1.1.2.1", service.getServiceNumber());
        assertEquals("Parent Service 12h", service.getServiceName());
    }

    @Test
    @DisplayName("Should not move service when overtimeHourTotais is zero")
    void shouldNotMoveWhenOvertimeHourTotaisIsZero() {
        var parent = ServiceLine.builder()
                .id("parent-id")
                .name("Parent Service 12h")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .build();

        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service 24h")
                .genericNumber("1.1.1.2.3")
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("parent-id")
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service 24h")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of())
                .overtimeHourTotais(Duration.ZERO)
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(parent, child);

        strategy.mutate(working, ppuServices);

        assertEquals("child-id", service.getServiceID());
        assertEquals("1.1.1.2.3", service.getServiceNumber());
        assertEquals("Child Service 24h", service.getServiceName());
    }

    @Test
    @DisplayName("Should not move service when it has no parent")
    void shouldNotMoveWhenServiceHasNoParent() {
        var service = RDOServiceEntity.builder()
                .serviceID("service-id")
                .serviceName("Service")
                .serviceNumber("1.1.1.2.1")
                .overtimes(List.of(new EmployeeOvertime(LocalTime.of(18, 0), LocalTime.of(19, 0))))
                .build();

        var ppuService = ServiceLine.builder()
                .id("service-id")
                .name("Service")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .parentId(null)
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(ppuService);

        strategy.mutate(working, ppuServices);

        assertEquals("service-id", service.getServiceID());
        assertEquals("1.1.1.2.1", service.getServiceNumber());
    }

    @Test
    @DisplayName("Should not move service when parent does not exist in PPU")
    void shouldNotMoveWhenParentNotFoundInPPU() {
        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service")
                .genericNumber("1.1.1.2.3")
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("non-existent-parent-id")
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of(new EmployeeOvertime(LocalTime.of(18, 0), LocalTime.of(19, 0))))
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(child);

        strategy.mutate(working, ppuServices);

        assertEquals("child-id", service.getServiceID());
        assertEquals("1.1.1.2.3", service.getServiceNumber());
    }

    @Test
    @DisplayName("Should handle overtimes with same start and end time")
    void shouldNotMoveWhenOvertimeStartEqualsEnd() {
        var parent = ServiceLine.builder()
                .id("parent-id")
                .name("Parent Service 12h")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .build();

        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service 24h")
                .genericNumber("1.1.1.2.3")
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("parent-id")
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service 24h")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of(new EmployeeOvertime(LocalTime.of(18, 0), LocalTime.of(18, 0))))
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(parent, child);

        strategy.mutate(working, ppuServices);

        assertEquals("child-id", service.getServiceID());
        assertEquals("1.1.1.2.3", service.getServiceNumber());
    }

    @Test
    @DisplayName("Should handle null or empty inputs gracefully")
    void shouldHandleNullOrEmptyInputsGracefully() {
        assertDoesNotThrow(() -> strategy.mutate(null, List.of()));
        assertDoesNotThrow(() -> strategy.mutate(List.of(), null));
        assertDoesNotThrow(() -> strategy.mutate(List.of(), List.of()));
    }

    @Test
    @DisplayName("Should handle overnight overtime periods")
    void shouldMoveWhenOvertimeCrossesMidnight() {
        var parent = ServiceLine.builder()
                .id("parent-id")
                .name("Parent Service 12h")
                .genericNumber("1.1.1.2.1")
                .type(ServiceType.NORMAL)
                .build();

        var child = ServiceLine.builder()
                .id("child-id")
                .name("Child Service 24h")
                .genericNumber("1.1.1.2.3")
                .type(ServiceType.VINTEQUATROHORAS)
                .parentId("parent-id")
                .timeStrategy(TimeStrategyType.MOVE_OUT)
                .build();

        var service = RDOServiceEntity.builder()
                .serviceID("child-id")
                .serviceName("Child Service 24h")
                .serviceNumber("1.1.1.2.3")
                .overtimes(List.of(new EmployeeOvertime(LocalTime.of(23, 0), LocalTime.of(1, 0))))
                .build();

        var working = new ArrayList<>(List.of(service));
        var ppuServices = List.of(parent, child);

        strategy.mutate(working, ppuServices);

        assertEquals("parent-id", service.getServiceID());
        assertEquals("1.1.1.2.1", service.getServiceNumber());
        assertEquals("Parent Service 12h", service.getServiceName());
    }
}

