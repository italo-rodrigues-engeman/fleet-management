package com.indux.modules.ppu.application.services.fixtures;

import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.services.ppu.total_balance.TotalBalanceFixture;
import com.indux.modules.ppu.domain.entities.item.EquipmentEntity;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.item.PPUType;
import com.indux.modules.ppu.domain.entities.item.ServicePosition;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PPUFixture {

    public static final MeasurementForecast fakeForecast = new MeasurementForecast(
            "ID-FORECAST",
            "PRA-1",
            5
    );

    public static final ServiceLine fakePPUServiceItem = ServiceLine.builder()
            .id("TESTE")
            .genericNumber("1.1.101")
            .name("Nome do Serviço")
            .unitOfMeasurement("UN")
            .value(10.0)
            .factor(1.0)
            .measurementForecasts(List.of(PPUFixture.fakeForecast))
            .positions(List.of())
            .disposicao(false)
            .isOvertimeService(false)
            .ppuNumber("1.1")
            .build();

    public static final ServicePosition fakePosition = new ServicePosition(
           "Cargo_Nome",
            "2220"
    );

    public static final ServiceLine fakeServiceLineWithPositions = ServiceLine.builder()
            .name("Servico - 123")
            .positions(List.of(fakePosition))
            .disposicao(false)
            .build();

    public static final EquipmentEntity fakeEquipmentEntity = new EquipmentEntity(
            "equipamento-123",
            "Patrimonio",
            "Fabricante",
            "Modelo",
            "PRA-1"
    );
    public static final EquipmentLine fakeEquipmentService = EquipmentLine
            .builder()
            .id("ID-EQUIPAMENTO")
            .genericNumber("EQUIP-001")
            .ppuNumber("EQUIP-001")
            .name("Guindaste Teste")
            .measurementForecasts(List.of(fakeForecast))
            .equipments(List.of(fakeEquipmentEntity))
            .unitOfMeasurement("UN")
            .value(500.0)
            .factor(1.0).build();


    public static final SteelCableLine fakeSteelCableItem =  SteelCableLine
            .builder()
            .id("ID")
            .genericNumber("123456789")
            .ppuNumber("1.2.3")
            .name("EQUIP-001")
            .unitOfMeasurement("UN")
            .value(500.0)
            .factor(1.0)
            .certificate("certificado-102")
            .platforms(List.of("PRA-1"))
            .totalPlanned(3)
            .build();

    public static final AccessoryKitLine fakeAccessoryKitItem =  AccessoryKitLine.builder()
            .id("kit-123")
            .genericNumber("1.0.0.01")
            .ppuNumber("1.0")
            .name("Kit de acessórios de teste")
            .model("modelo")
            .unitOfMeasurement("UN")
            .value(100.0)
            .factor(1.0)
            .measurementForecasts(List.of(fakeForecast))
            .build();


    public static final PPUEntity fakePPUEntity = PPUEntity.builder()
            .id("ID")
            .codeID(1L)
            .regionalId(1L)
            .regionalNome("RJ")
            .type(PPUType.MOVIMENTO_CARGAS)
            .responsible("ID-RESPONSAVEL")
            .dateRange(new DateRange())
            .contract(Map.of())
            .contractId(2L)
            .platforms(List.of("PRA-1", "PRA-2", "P-01"))
            .platformFrequencies(null)
            .clientId(7L)
            .generalObservation("Observações de teste")
            .status(DocumentStatus.ABERTO)
            .signalmenQuantity(2)
            .mandatorySAMC(true)
            .auditableConfig(null)
            .createdBy("Teste")
            .createdAt(LocalDateTime.now())
            .updatedBy("Usuário de Teste")
            .updatedAt(LocalDateTime.now())
            .services(new ArrayList<>(List.of(fakePPUServiceItem)))
            .equipments(List.of(fakeEquipmentService))
            .steelCables(List.of(fakeSteelCableItem))
            .accessoryKits(List.of(fakeAccessoryKitItem))
            .craneControls(new ArrayList<>())
            .steelCableControls(new ArrayList<>())
            .shiftSchedule(new ArrayList<>())
            .teamLeader(new ArrayList<>())
            .measurementPeriod(null)
            .lines(new ArrayList<>())
            .version(1L)
            .is24hType(true)
            .nickname("PPU Teste")
            .signalmenValue(15.0)
            .totalBalances(List.of())
            .projectId(0L)
            .availableType(List.of())
            .hasSupervisorOnBoard(true)
            .allowsRDODuplication(true)
            .build();

    public static PPUEntity fakePPUEntityFunction() {
        return PPUEntity.builder()
                .id("ID")
                .codeID(1L)
                .regionalId(1L)
                .regionalNome("RJ")
                .type(PPUType.MOVIMENTO_CARGAS)
                .responsible("ID-RESPONSAVEL")
                .dateRange(new DateRange())
                .contract(Map.of())
                .contractId(2L)
                .platforms(List.of("PRA-1", "PRA-2", "P-01"))
                .platformFrequencies(null)
                .clientId(7L)
                .generalObservation("Observações de teste")
                .status(DocumentStatus.ABERTO)
                .signalmenQuantity(2)
                .mandatorySAMC(true)
                .auditableConfig(null)
                .createdBy("Teste")
                .createdAt(LocalDateTime.now())
                .updatedBy("Usuário de Teste")
                .updatedAt(LocalDateTime.now())
                .services(new ArrayList<>(List.of(fakePPUServiceItem)))
                .equipments(List.of(fakeEquipmentService))
                .steelCables(List.of(fakeSteelCableItem))
                .accessoryKits(List.of(fakeAccessoryKitItem))
                .craneControls(new ArrayList<>())
                .steelCableControls(new ArrayList<>())
                .shiftSchedule(new ArrayList<>())
                .teamLeader(new ArrayList<>())
                .measurementPeriod(null)
                .lines(new ArrayList<>())
                .version(1L)
                .is24hType(true)
                .nickname("PPU Teste")
                .signalmenValue(15.0)
                .totalBalances(TotalBalanceFixture.createTotalBalanceList())
                .projectId(0L)
                .availableType(List.of())
                .hasSupervisorOnBoard(true)
                .allowsRDODuplication(true)
                .build();
    }
}
