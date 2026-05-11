package com.indux.modules.ppu.infra.mapper.bm;

import com.indux.modules.ppu.application.dtos.response.bm.*;
import com.indux.modules.ppu.application.projection.BMProjection;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.ppu.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface BMMapper {

    @Mapping(source = "genericNumber", target = "number")
    BMServiceReportItem toBMServiceReportItem(ServiceLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMServiceReportItem toBMServiceReportItem(EquipmentLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMServiceReportItem toBMServiceReportItem(SteelCableLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMServiceReportItem toBMServiceReportItem(AccessoryKitLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMServiceReportItem toBMServiceReportItem(LinePPU entity);

    @Mapping(source = "entity.value", target = "unitValue")
    @Mapping(source = "entity.unitOfMeasurement", target = "unit")
    @Mapping(source = "entity.isOvertimeService", target = "isOvertimeService")
    @Mapping(source = "entity.children", target = "children")
    @Mapping(source = "entity.genericNumber", target = "number")
    @Mapping(source = "platform", target = "platform")
    @Mapping(source = "entity.overtimeService", target = "overtimeService")
    BMTimeline toBMTimeline(ServiceLine entity, String platform);

    @Mapping(source = "entity.value", target = "unitValue")
    @Mapping(source = "entity.unitOfMeasurement", target = "unit")
    @Mapping(source = "entity.genericNumber", target = "number")
    @Mapping(source = "platform", target = "platform")
    BMTimeline toBMTimeline(EquipmentLine entity, String platform);

    @Mapping(source = "entity.value", target = "unitValue")
    @Mapping(source = "entity.unitOfMeasurement", target = "unit")
    @Mapping(source = "entity.genericNumber", target = "number")
    @Mapping(source = "platform", target = "platform")
    BMTimeline toBMTimeline(AccessoryKitLine entity, String platform);

    @Mapping(source = "entity.value", target = "unitValue")
    @Mapping(source = "entity.unitOfMeasurement", target = "unit")
    @Mapping(source = "entity.genericNumber", target = "number")
    @Mapping(source = "platform", target = "platform")
    BMTimeline toBMTimeline(LinePPU entity, String platform);

    @Mapping(source = "entity.value", target = "unitValue")
    @Mapping(source = "entity.unitOfMeasurement", target = "unit")
    @Mapping(source = "entity.genericNumber", target = "number")
    @Mapping(source = "platform", target = "platform")
    BMTimeline toBMTimeline(SteelCableLine entity, String platform);

    @Mapping(source = "genericNumber", target = "number")
    BMMonthlyItem toBMMonthlyItem(ServiceLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMMonthlyItem toBMMonthlyItem(AccessoryKitLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMMonthlyItem toBMMonthlyItem(LinePPU entity);

    @Mapping(source = "genericNumber", target = "number")
    BMMonthlyItem toBMMonthlyItem(EquipmentLine entity);

    @Mapping(source = "genericNumber", target = "number")
    BMMonthlyItem toBMMonthlyItem(SteelCableLine entity);

    BMEntity fromModel(BMModel model);

    BMModel toModel(BMEntity entity);

    @Mapping(target = "id", source = "bm.id")
    @Mapping(target = "contrato", source = "ppu.contractName")
    @Mapping(target = "ppu", source = "bm.ppu")
    @Mapping(target = "ppuApelido", source = "ppu.nickname")
    @Mapping(target = "periodo", source = "bm.period")
    @Mapping(target = "regional", source = "ppu.regionalNome")
    @Mapping(target = "status", source = "bm.status")
    @Mapping(target = "criadoPor", source = "bm.createdBy")
    @Mapping(target = "criadoEm", source = "bm.createdAt")
    @Mapping(target = "porcentagem", source = "percentage")
    BMGrid toGrid(BMProjection bm, PPUEnhancedGridProjection ppu, BigDecimal percentage);
}
