package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.response.bm.BMTimeline;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.strategy.types.LineStrategyType;
import com.indux.modules.ppu.domain.strategy.time.types.TimeStrategyType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BMFixtures {

    public static MeasurementForecast forecastForPlatform(String platform, Integer total) {
        return MeasurementForecast
                .builder()
                .platform(platform)
                .total(total)
                .build();
    }

    public static ServiceLine createFakeServiceLineToBM_1() {
        var forecasts = new ArrayList<MeasurementForecast>();
        forecasts.add(forecastForPlatform("P-TESTE", 10));
        
        return ServiceLine
                .builder()
                .id("495ba9cd-5168-4236-b8dd-6c5f8b7527c0")
                .genericNumber("1.0.100")
                .name("Serviço de Supervisão de Movimentação de Cargas")
                .unitOfMeasurement("US")
                .value(500.0)
                .disposicao(false)
                .isOvertimeService(false)
                .measurementForecasts(forecasts)
                .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                .children(List.of("1.0.101", "1.0.102"))
                .build();
    }

    public static ServiceLine createFakeServiceLineToBM_2() {
        var forecasts = new ArrayList<MeasurementForecast>();
        forecasts.add(forecastForPlatform("P-TESTE", 0));
        
        return ServiceLine
                .builder()
                .id("efe5955c-4c0a-4b30-9e20-a178b9f1bf5c")
                .genericNumber("1.0.101")
                .name("Serviço de Supervisão de Movimentação de Cargas à Disposição")
                .unitOfMeasurement("US")
                .value(250.0)
                .disposicao(true)
                .isOvertimeService(false)
                .measurementForecasts(forecasts)
                .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                .build();
    }

    public static ServiceLine createFakeServiceLineToBM_3() {
        var forecasts = new ArrayList<MeasurementForecast>();
        forecasts.add(forecastForPlatform("P-TESTE", 0));
        
        return ServiceLine
                .builder()
                .id("15e1d2c7-3a79-4470-bcbe-d9801ba206c6")
                .genericNumber("1.0.102")
                .name("Serviço Extraordinário de Supervisão de Movimentação de Cargas")
                .unitOfMeasurement("HR")
                .value(100.0)
                .disposicao(false)
                .isOvertimeService(true)
                .measurementForecasts(forecasts)
                .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                .build();
    }

    public static ServiceLine createFakeServiceLineToBM_4() {
        var forecasts = new ArrayList<MeasurementForecast>();
        forecasts.add(forecastForPlatform("P-TESTE", 10));
        
        return ServiceLine
                .builder()
                .id("2df2181f-e99b-440c-9b5a-45a9423e3365")
                .genericNumber("1.1.110")
                .name("Serviço de Auxiliar de Movimentação de Cargas")
                .unitOfMeasurement("US")
                .value(300.0)
                .disposicao(false)
                .isOvertimeService(false)
                .measurementForecasts(forecasts)
                .children(List.of("1.1.111", "1.1.112"))
                .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                .build();
    }

    public static ServiceLine createFakeServiceLineToBM_5() {
        var forecasts = new ArrayList<MeasurementForecast>();
        forecasts.add(forecastForPlatform("P-TESTE", 0));
        
        return ServiceLine
                .builder()
                .id("759738d0-98f5-47e4-8e0a-a4a383f2bfa6")
                .genericNumber("1.1.111")
                .name("Serviço de Auxiliar de Movimentação de Cargas à Disposição")
                .unitOfMeasurement("US")
                .value(150.0)
                .disposicao(true)
                .isOvertimeService(false)
                .measurementForecasts(forecasts)
                .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                .build();
    }
    
    public static ServiceLine createFakeServiceLineToBM_6() {
        var forecasts = new ArrayList<MeasurementForecast>();
        forecasts.add(forecastForPlatform("P-TESTE", 0));
        
        return ServiceLine
                .builder()
                .id("52676105-5c28-47d9-a562-2f44b01ba967")
                .genericNumber("1.1.112")
                .name("Serviço Extraordinário de Auxiliar de Movimentação de Cargas")
                .unitOfMeasurement("HR")
                .value(50.0)
                .disposicao(false)
                .isOvertimeService(true)
                .measurementForecasts(forecasts)
                .lineStrategy(LineStrategyType.UNIQUE)
                .timeStrategy(TimeStrategyType.OVERTIME_ANOTHER_LINE)
                .build();
    }
    
    public static PPUEntity createFakePPUToBMTimeline() {
        var platforms = new ArrayList<String>();
        platforms.add("P-TESTE");
        
        return PPUEntity
                .builder()
                .contractId(313L)
                .platforms(platforms)
                .services(List.of(
                        createFakeServiceLineToBM_1(),
                        createFakeServiceLineToBM_2(),
                        createFakeServiceLineToBM_3(),
                        createFakeServiceLineToBM_4(),
                        createFakeServiceLineToBM_5(),
                        createFakeServiceLineToBM_6()
                ))
                .projectId(44L)
                .status(DocumentStatus.ABERTO)
                .build();
    }

    public static BMTimeline createBMTimeLine1() {
        return BMTimeline
                .builder()
                .id("495ba9cd-5168-4236-b8dd-6c5f8b7527c0")
                .number("1.0.100")
                .name("Serviço de Supervisão de Movimentação de Cargas")
                .unit("US")
                .overtimeService("15e1d2c7-3a79-4470-bcbe-d9801ba206c6")
                .isOvertimeService(false)
                .children(List.of("1.0.101", "1.0.102"))
                .qtdExpected(10)
                .unitValue(BigDecimal.valueOf(500.00))
                .build();
    }

    public static BMTimeline createBMTimeLineChild1() {
        return BMTimeline
                .builder()
                .id("efe5955c-4c0a-4b30-9e20-a178b9f1bf5c")
                .number("1.0.101")
                .name("Serviço de Supervisão de Movimentação de Cargas à Disposição")
                .unit("US")
                .isOvertimeService(false)
                .children(List.of())
                .qtdExpected(0)
                .unitValue(BigDecimal.valueOf(250.00))
                .build();
    }

    public static BMTimeline createBMTimeLineChild2() {
        return BMTimeline
                .builder()
                .id("15e1d2c7-3a79-4470-bcbe-d9801ba206c6")
                .number("1.0.102")
                .name("Serviço Extraordinário de Supervisão de Movimentação de Cargas")
                .unit("HR")
                .isOvertimeService(true)
                .children(List.of())
                .qtdExpected(0)
                .unitValue(BigDecimal.valueOf(100.00))
                .build();
    }

    public static BMTimeline createBMTimeLineChild4() {
        return BMTimeline
                .builder()
                .id("2df2181f-e99b-440c-9b5a-45a9423e3365")
                .number("1.1.111")
                .name("Serviço de Auxiliar de Movimentação de Cargas")
                .unit("US")
                .isOvertimeService(false)
                .children(List.of())
                .overtimeService("52676105-5c28-47d9-a562-2f44b01ba967")
                .qtdExpected(0)
                .unitValue(BigDecimal.valueOf(100.00))
                .build();
    }

    public static BMTimeline createBMTimeLineChild5() {
        return BMTimeline
                .builder()
                .id("759738d0-98f5-47e4-8e0a-a4a383f2bfa6")
                .number("1.1.112")
                .name("Serviço de Auxiliar de Movimentação de Cargas à Disposição")
                .unit("US")
                .isOvertimeService(false)
                .children(List.of())
                .qtdExpected(0)
                .unitValue(BigDecimal.valueOf(150.00))
                .build();
    }

    public static BMTimeline createBMTimeLineChild6() {
        return BMTimeline
                .builder()
                .id("52676105-5c28-47d9-a562-2f44b01ba967")
                .number("1.1.113")
                .name("Serviço Extraordinário de Auxiliar de Movimentação de Cargas")
                .unit("HR")
                .isOvertimeService(true)
                .children(List.of())
                .qtdExpected(0)
                .unitValue(BigDecimal.valueOf(50.00))
                .build();
    }

    public static RDOServiceEntity createFakeRDOServiceForBM(String id, Duration overtimeTotal) {
        return RDOServiceEntity
                .builder()
                .serviceID(id)
                .overtimeHourTotais(overtimeTotal)
                .valueMeasured(1.0)
                .build();
    }

    public static RDOEntity createFakeRDOForBM(List<RDOServiceEntity> services) {
        return RDOEntity
                .builder()
                .id("rdo-1")
                .platform("P-TESTE")
                .date(LocalDate.of(2025, 10, 3))
                .services(services)
                .statusOP(RDOStatusOP.APPROVED)
                .build();
    }

    public static BMTimeline.DailyEntry createDailyEntry(LocalDate date, Integer day, Number quantity) {
        return new BMTimeline.DailyEntry(date, day, quantity);
    }

    public static PPUEntity createMultiPlatformPPU() {
        var ppu = createFakePPUToBMTimeline();
        ppu.getPlatforms().add("P-PROD");
        
        var services = ppu.getServices();
        services.get(0).getMeasurementForecasts().add(forecastForPlatform("P-PROD", 5));
        services.get(3).getMeasurementForecasts().add(forecastForPlatform("P-PROD", 8));
        
        return ppu;
    }
}