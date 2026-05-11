package com.indux.modules.ppu.application.services.rdo.helper;

import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.domain.entities.rdo.SteelCableChecker;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.ppu.AccessoryKitLine;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.ppu.SteelCableLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class RDOTotalPlannedHelper {

    public static void enrichServiceWithPlanned(String platform, List<RDOServiceEntity> rdoServices, List<ServiceLine> serviceItems) {
        if (rdoServices == null || serviceItems == null || rdoServices.isEmpty() || serviceItems.isEmpty()) return;

        for (RDOServiceEntity service : rdoServices) {
            var matchingService = serviceItems.stream()
                    .filter(item -> item.getId().equals(service.getServiceID()))
                    .findFirst()
                    .orElse(null);

            int total = Optional.ofNullable(matchingService)
                    .flatMap(ms -> ms.getMeasurementForecasts().stream()
                            .filter(f -> f.getPlatform().equals(platform))
                            .findFirst())
                    .map(MeasurementForecast::getTotal)
                    .orElse(0);

            service.setTotalPlanned(total);
        }
    }

    public static Integer enrichEquipmentWithPlanned(String platform, List<EquipmentLine> ppuEquipments) {
        if (ppuEquipments == null || ppuEquipments.isEmpty()) return 0;

        EquipmentLine firstEquipment = ppuEquipments.stream().findFirst().orElse(null);
        if (firstEquipment.getMeasurementForecasts() == null) return 0;

        var forecast = firstEquipment.getMeasurementForecasts().stream()
                .filter(e -> platform != null && platform.equals(e.getPlatform()))
                .findFirst()
                .orElse(null);

        if (forecast == null) return 0;

        return forecast.getTotal();
    }

    public static List<SteelCableChecker> enrichSteelCableWithPlanned(String platform, List<SteelCableChecker> rdoCables, List<SteelCableLine> ppuCables) {
        if (rdoCables == null || ppuCables == null || rdoCables.isEmpty() || ppuCables.isEmpty()) return new ArrayList<>();

        List<SteelCableLine> filtered = ppuCables.stream()
                .filter(e -> e.getPlatforms() != null && e.getPlatforms().contains(platform))
                .toList();

        return rdoCables.stream()
                .map(item -> {
                    int total = filtered.stream()
                            .filter(ppu -> ppu.getId().equals(item.id()))
                            .findFirst()
                            .map(SteelCableLine::getTotalPlanned)
                            .orElse(0);
                    return item.copyWithTotalPlanned(total);
                })
                .toList();
    }

    public static List<AccessoryKitDTO> enrichAccessoryKitsWithPlanned(String platform, List<AccessoryKitDTO> rdoKits, List<AccessoryKitLine> ppuKits) {
        if (rdoKits == null || ppuKits == null || rdoKits.isEmpty() || ppuKits.isEmpty()) return new ArrayList<>();

        return rdoKits.stream()
                .map(item -> {
                    int total = ppuKits.stream()
                            .filter(ppu -> ppu.getId().equals(item.id()))
                            .findFirst()
                            .flatMap(ppu -> ppu.getMeasurementForecasts().stream()
                                    .filter(f -> f.getPlatform().equals(platform))
                                    .findFirst())
                            .map(MeasurementForecast::getTotal)
                            .orElse(0);
                    return item.copyWithTotalPlanned(total);
                })
                .toList();
    }
}
