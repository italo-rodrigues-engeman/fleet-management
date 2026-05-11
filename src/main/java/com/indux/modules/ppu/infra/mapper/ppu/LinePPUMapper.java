package com.indux.modules.ppu.infra.mapper.ppu;

import com.indux.modules.ppu.application.dtos.item.LineDTO;
import com.indux.modules.ppu.application.dtos.response.lines.LinePPUResponse;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LinePPUMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "idOrRandom")
    @Mapping(target = "genericNumber", source = "numero")
    @Mapping(target = "ppuNumber", source = "numeroPPU")
    @Mapping(target = "name", source = "nome")
    @Mapping(target = "unitOfMeasurement", source = "unidadeMedida")
    @Mapping(target = "value", source = "valor")
    @Mapping(target = "factor", source = "fator")
    @Mapping(target = "measurementForecasts", source = "totalPrevisto", qualifiedByName = "listOrEmpty")
    @Mapping(target = "platforms", source = "plataformas")
    @Mapping(target = "auditableLines", source = ".", qualifiedByName = "resolveAuditableLines")
    @Mapping(target = "active", ignore = true)
    LinePPU toEntity(LineDTO dto);

    List<LinePPU> toEntity(List<LineDTO> dto);

    @Mapping(target = "auditableLine", expression = "java(entity.resolveAuditableLine())")
    @Mapping(target = "auditableLines", source = "auditableLines")
    LinePPUResponse toResponse(LinePPU entity);

    @Named("idOrRandom")
    default String idOrRandom(String id) {
        return id != null ? id : UUID.randomUUID().toString();
    }

    @Named("listOrEmpty")
    default <T> List<T> listOrEmpty(List<T> list) {
        return list != null ? list : List.of();
    }

    @Named("resolveAuditableLines")
    default List<AuditableLineConfig> resolveAuditableLines(LineDTO dto) {
        if (dto.linhasAuditaveis() != null && !dto.linhasAuditaveis().isEmpty()) {
            return dto.linhasAuditaveis();
        }
        if (dto.linhaAuditavel() != null && !dto.linhaAuditavel().isBlank()) {
            return List.of(AuditableLineConfig.builder().label(dto.linhaAuditavel()).build());
        }
        return null;
    }
}
