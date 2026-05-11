package com.indux.modules.ppu.infra.mapper.ppu;

import com.indux.modules.ppu.application.dtos.item.ServiceItemDTO;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = "spring",
        imports = {UUID.class, Optional.class, List.class, ArrayList.class})
public interface ServiceLineMapper {

    @Mapping(target = "id", expression = "java(Optional.ofNullable(dto.id()).orElse(UUID.randomUUID().toString()))")
    @Mapping(target = "genericNumber", source = "numero")
    @Mapping(target = "ppuNumber", source = "numeroPPU")
    @Mapping(target = "name", source = "nome")
    @Mapping(target = "unitOfMeasurement", source = "unidadeMedida")
    @Mapping(target = "value", source = "valor")
    @Mapping(target = "factor", source = "fator")
    @Mapping(target = "positions", ignore = true)
    @Mapping(target = "employees", expression = "java(Optional.ofNullable(dto.colaboradores()).orElse(List.of()))")
    @Mapping(target = "measurementForecasts", expression = "java(Optional.ofNullable(dto.totalPrevisto()).orElse(List.of()))")
    @Mapping(target = "children", source = "filhos")
    @Mapping(target = "isOvertimeService", source = "servicoHoraExtra")
    @Mapping(target = "type", source = "tipo")
    @Mapping(target = "parentId", source = "linhaPai")
    @Mapping(target = "auditableLines", source = ".", qualifiedByName = "resolveServiceAuditableLines")
    @Mapping(target = "lineStrategy", source = "lineStrategy")
    @Mapping(target = "timeStrategy", source = "timeStrategy")
    @Mapping(target = "distributionPercentage", source = "distributionPercentage")
    @Mapping(target = "overtimeService", source = "linhaHoraExtraId")
    @Mapping(target = "platforms", ignore = true)
    @Mapping(target = "active", ignore = true)
    ServiceLine toEntity(ServiceItemDTO dto);

    List<ServiceLine> toEntityList(List<ServiceItemDTO> items);

    @Named("resolveServiceAuditableLines")
    default List<AuditableLineConfig> resolveServiceAuditableLines(ServiceItemDTO dto) {
        if (dto.linhasAuditaveis() != null && !dto.linhasAuditaveis().isEmpty()) {
            return dto.linhasAuditaveis();
        }
        if (dto.linhaAuditavel() != null && !dto.linhaAuditavel().isBlank()) {
            return List.of(AuditableLineConfig.builder().label(dto.linhaAuditavel()).build());
        }
        return null;
    }
}