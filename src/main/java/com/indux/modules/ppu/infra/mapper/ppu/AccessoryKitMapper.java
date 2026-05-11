package com.indux.modules.ppu.infra.mapper.ppu;

import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.domain.entities.ppu.AccessoryKitLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring",
        imports = { UUID.class, Optional.class, Collections.class, AccessoryKitLine.class }
)
public interface AccessoryKitMapper {

    @Mapping(target = "id", expression = "java(Optional.ofNullable(dto.id()).orElse(UUID.randomUUID().toString()))")
    @Mapping(source = "valorItem", target = "value")
    @Mapping(source = "numero", target = "genericNumber")
    @Mapping(source = "numeroPPU", target = "ppuNumber")
    @Mapping(source = "fatorItem", target = "factor")
    @Mapping(source = "unidadeMedida", target = "unitOfMeasurement")
    @Mapping(source = "nome", target = "name")
    @Mapping(source = "descricaoModelo", target = "model")
    @Mapping(target = "measurementForecasts", expression = "java(Optional.ofNullable(dto.totalPrevisto()).orElse(Collections.emptyList()))")
    AccessoryKitLine toEntity(AccessoryKitDTO dto);
}
