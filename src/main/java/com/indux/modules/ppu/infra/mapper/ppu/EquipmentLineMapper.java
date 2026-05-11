package com.indux.modules.ppu.infra.mapper.ppu;

import com.indux.modules.ppu.application.dtos.item.EquipmentServiceDTO;
import com.indux.modules.ppu.domain.entities.item.EquipmentEntity;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring",
        imports = {UUID.class, Optional.class, Collections.class, EquipmentEntity.class}
)
public interface EquipmentLineMapper {

    @Mapping(target = "id", expression = "java(Optional.ofNullable(dto.id()).orElse(UUID.randomUUID().toString()))")
    @Mapping(source = "valor", target = "value")
    @Mapping(source = "fator", target = "factor")
    @Mapping(source = "numeroPPU", target = "ppuNumber")
    @Mapping(source = "unidadeMedida", target = "unitOfMeasurement")
    @Mapping(source = "numero", target = "genericNumber")
    @Mapping(source = "nome", target = "name")
    @Mapping(target = "equipments", expression = "java(Optional.ofNullable(dto.equipamentos()).orElse(Collections.emptyList()).stream().map(EquipmentEntity::fromDTO).toList())")
    @Mapping(target = "measurementForecasts", expression = "java(Optional.ofNullable(dto.totalPrevisto()).orElse(Collections.emptyList()))")
    @Mapping(target = "equipmentType", source = "tipoEquipamento")
    EquipmentLine toEntity(EquipmentServiceDTO dto);
}
