package com.indux.modules.ppu.infra.mapper;

import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.RDOFrequencyDTO;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.infra.mapper.ppu.LinePPUMapper;
import com.indux.modules.ppu.infra.mapper.response.*;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {
                ServiceLineResponseMapper.class,
                EquipmentLineResponseMapper.class,
                SteelCableLineResponseMapper.class,
                AccessoryKitLineResponseMapper.class,
                CraneControlResponseMapper.class,
                SteelCableControlResponseMapper.class,
                ShiftScheduleResponseMapper.class,
                TeamLeaderResponseMapper.class,
                MeasurementPeriodResponseMapper.class,
                TotalBalanceResponseMapper.class,
                EquipmentEntityResponseMapper.class,
                LinePPUMapper.class
        }
)
public abstract class PPUResponseMapper {

    @Autowired
    protected ServiceLineResponseMapper serviceLineMapper;
    @Autowired
    protected EquipmentLineResponseMapper equipmentLineMapper;
    @Autowired
    protected SteelCableLineResponseMapper steelCableLineMapper;
    @Autowired
    protected AccessoryKitLineResponseMapper accessoryKitLineMapper;
    @Autowired
    protected CraneControlResponseMapper craneControlMapper;
    @Autowired
    protected SteelCableControlResponseMapper steelCableControlMapper;
    @Autowired
    protected EquipmentEntityResponseMapper equipmentEntityMapper;
    @Autowired
    protected LinePPUMapper linePPUMapper;

    @AfterMapping
    protected void applyFiltersAndMappings(@MappingTarget PPUResponse response,
                                           PPUEntity entity,
                                           @Context String platform) {
        if (entity == null) {
            return;
        }

        // Definir campos extras
        if (platform != null) {
            response.setCurrentPlatform(platform);
        }

        if (entity.getPlatformFrequencies() != null) {
            Map<String, RDOFrequencyDTO> dtoFrequencies = entity.getPlatformFrequencies().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> RDOFrequencyDTO.fromDomain(entry.getValue())
                    ));
            response.setPlatformFrequencies(dtoFrequencies);
        }

        PPUResponseFilterHelper.applyFilters(response, entity, platform,
                serviceLineMapper, equipmentLineMapper, steelCableLineMapper,
                accessoryKitLineMapper, craneControlMapper, steelCableControlMapper,
                equipmentEntityMapper, linePPUMapper);
    }

    @Mapping(target = "user", source = "user")
    @Mapping(target = "currentPlatform", ignore = true)
    @Mapping(target = "boardedEmployees", source = "boardedEmployees")
    @Mapping(target = "inLandingDayEmployees", source = "inLandingDayEmployees")
    @Mapping(target = "rdoCodeSequence", source = "rdoCodeSequence")
    @Mapping(target = "contract", source = "entity.contract")
    @Mapping(target = "platformFrequencies", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "equipments", ignore = true)
    @Mapping(target = "steelCables", ignore = true)
    @Mapping(target = "accessoryKits", ignore = true)
    @Mapping(target = "craneControls", ignore = true)
    @Mapping(target = "steelCableControls", ignore = true)
    @Mapping(target = "lines", ignore = true)
    public abstract PPUResponse toResponse(PPUEntity entity,
                                           @Context String platform,
                                           SimpleEmployeeDTO user,
                                           List<BoardedEmployee> boardedEmployees,
                                           List<BoardedEmployee> inLandingDayEmployees,
                                           Long rdoCodeSequence);
}
