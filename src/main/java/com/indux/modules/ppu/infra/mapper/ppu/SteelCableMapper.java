package com.indux.modules.ppu.infra.mapper.ppu;

import com.indux.modules.ppu.application.dtos.item.SteelCableDTO;
import com.indux.modules.ppu.domain.entities.ppu.SteelCableLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = { UUID.class, Optional.class, List.class, ArrayList.class })
public interface SteelCableMapper {

    @Mapping(target = "id", expression = "java(Optional.ofNullable(dto.id()).orElse(UUID.randomUUID().toString()))")
    @Mapping(source = "numero", target = "genericNumber")
    @Mapping(target ="ppuNumber", source = "numeroPPU")
    @Mapping(source = "nome", target = "name")
    @Mapping(source = "unidadeMedida", target = "unitOfMeasurement")
    @Mapping(source = "valorItem", target = "value")
    @Mapping(source = "fatorItem", target = "factor")
    @Mapping(source = "certificadoUri", target = "certificate")
    @Mapping(source = "plataformas", target = "platforms")
    @Mapping(source = "totalPrevisto", target = "totalPlanned")
    SteelCableLine toEntity(SteelCableDTO dto);
}
