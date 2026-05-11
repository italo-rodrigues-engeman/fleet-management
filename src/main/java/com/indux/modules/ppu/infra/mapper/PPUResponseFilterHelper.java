package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.response.lines.EquipmentLineResponse;
import com.indux.modules.ppu.application.dtos.response.lines.ServiceLineResponse;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.infra.mapper.ppu.LinePPUMapper;
import com.indux.modules.ppu.infra.mapper.response.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class PPUResponseFilterHelper {

    private PPUResponseFilterHelper() {
        // Utility class
    }

    static void applyFilters(PPUResponse response,
                            PPUEntity entity,
                            String platform,
                            ServiceLineResponseMapper serviceLineMapper,
                            EquipmentLineResponseMapper equipmentLineMapper,
                            SteelCableLineResponseMapper steelCableLineMapper,
                            AccessoryKitLineResponseMapper accessoryKitLineMapper,
                            CraneControlResponseMapper craneControlMapper,
                            SteelCableControlResponseMapper steelCableControlMapper,
                            EquipmentEntityResponseMapper equipmentEntityMapper,
                             LinePPUMapper linePPUMapper) {
        
        // Filtrar serviços: remover isOvertimeService = true e filtrar por plataforma
        if (entity.getServices() != null) {
            var filteredServices = entity.getServices().stream()
                    .filter(PPUResponseFilterHelper::isLineActive)
                    .filter(s -> s.getIsOvertimeService() == null || !s.getIsOvertimeService())
                    .map(service -> {
                        var serviceResponse = serviceLineMapper.toResponse(service);
                        filterEmployeesByPlatform(serviceResponse, service, platform);
                        return serviceResponse;
                    })
                    .collect(Collectors.toList());
            response.setServices(filteredServices);
        }

        // Filtrar equipamentos por plataforma
        if (entity.getEquipments() != null) {
            var filteredEquipments = entity.getEquipments().stream()
                    .filter(PPUResponseFilterHelper::isLineActive)
                    .map(equipment -> {
                        var equipmentResponse = equipmentLineMapper.toResponse(equipment);
                        filterEquipmentEntitiesByPlatform(equipmentResponse, equipment, platform, equipmentEntityMapper);
                        return equipmentResponse;
                    })
                    .filter(eq -> eq.getEquipments() != null && !eq.getEquipments().isEmpty())
                    .collect(Collectors.toList());
            response.setEquipments(filteredEquipments);
        }

        // Filtrar steelCables por plataforma
        if (entity.getSteelCables() != null) {
            var filteredSteelCables = entity.getSteelCables().stream()
                    .filter(PPUResponseFilterHelper::isLineActive)
                    .filter(e -> e.getPlatforms() != null && e.getPlatforms().contains(platform))
                    .map(steelCableLineMapper::toResponse)
                    .collect(Collectors.toList());
            response.setSteelCables(filteredSteelCables);
        }

        // Filtrar accessoryKits
        if (entity.getAccessoryKits() != null) {
            var accessoryKits = entity.getAccessoryKits().stream()
                    .filter(PPUResponseFilterHelper::isLineActive)
                    .map(accessoryKitLineMapper::toResponse)
                    .collect(Collectors.toList());
            response.setAccessoryKits(accessoryKits);
        }

        if (entity.getLines() != null) {
            var lines = entity.getLines().stream()
                    .filter(PPUResponseFilterHelper::isLineActive)
                    .map(linePPUMapper::toResponse)
                    .collect(Collectors.toList());
            response.setLines(lines);
        }

        // Filtrar craneControls por plataforma
        if (entity.getCraneControls() != null) {
            var filteredCraneControls = entity.getCraneControls().stream()
                    .filter(e -> Objects.equals(platform, e.getPlatform()))
                    .map(craneControlMapper::toResponse)
                    .collect(Collectors.toList());
            response.setCraneControls(filteredCraneControls);
        }

        // Filtrar steelCableControls por plataforma
        if (entity.getSteelCableControls() != null) {
            var filteredSteelCableControls = entity.getSteelCableControls().stream()
                    .filter(e -> Objects.equals(platform, e.getPlatform()))
                    .map(steelCableControlMapper::toResponse)
                    .collect(Collectors.toList());
            response.setSteelCableControls(filteredSteelCableControls);
        }
    }

    private static void filterEmployeesByPlatform(ServiceLineResponse response,
                                                  ServiceLine service,
                                                  String platform) {
        if (service.getEmployees() != null) {
            var filteredEmployees = service.getEmployees().stream()
                    .filter(e -> e.getPlatform() != null && e.getPlatform().contains(platform))
                    .collect(Collectors.toList());
            response.setEmployees(filteredEmployees);
        } else {
            response.setEmployees(List.of());
        }
    }

    private static void filterEquipmentEntitiesByPlatform(EquipmentLineResponse response,
                                                          com.indux.modules.ppu.domain.entities.ppu.EquipmentLine equipment,
                                                          String platform,
                                                          EquipmentEntityResponseMapper equipmentEntityMapper) {
        if (equipment.getEquipments() != null) {
            var filteredEquipmentList = equipment.getEquipments().stream()
                    .filter(e -> e.getPlatform() != null && e.getPlatform().contains(platform))
                    .map(equipmentEntityMapper::toResponse)
                    .collect(Collectors.toList());
            response.setEquipments(filteredEquipmentList);
        } else {
            response.setEquipments(List.of());
        }
    }

    private static boolean isLineActive(LinePPU line) {
        return !Boolean.FALSE.equals(line.getActive());
    }
}
