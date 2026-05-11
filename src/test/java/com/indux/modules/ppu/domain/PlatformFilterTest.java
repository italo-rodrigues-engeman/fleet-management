package com.indux.modules.ppu.domain;


import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.services.PlatformFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlatformFilterTest {

    @Test
    @DisplayName("Should filter LINEPPU by platform")
    void filter() {
        var service = PlatformFilterFixtures.serviceLine(List.of("A", "B"));
        var platform = "A";

        var response = PlatformFilter.filter(List.of(service), platform);

        assertTrue(response.contains(service));
    }

    @Test
    @DisplayName("Should not find service because platform not is equal")
    void filter_not_found() {
        var service = PlatformFilterFixtures.serviceLine(List.of("A", "B"));
        var platform = "C";

        var response = PlatformFilter.filter(List.of(service), platform);

        assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Should filter EQUIPMENTLINE by platform")
    void filter_equipmentLine() {
        var equipment = PlatformFilterFixtures.equipmentLine(List.of("A", "B"));
        var platform = "A";
        var response = PlatformFilter.filter(List.of(equipment), platform);
        assertTrue(response.contains(equipment));
    }

    @Test
    @DisplayName("Should filter LINEPPUs by platform")
    void filterMore() {
        var service = PlatformFilterFixtures.serviceLine(List.of("A", "B"));
        var platform = "A";
        var response = PlatformFilter.filter(List.of(service, service, service, PlatformFilterFixtures.serviceLine(List.of("B"))), platform);

        assertTrue(response.contains(service));
        assertEquals(3, response.size());

    }

}

class PlatformFilterFixtures{
    public static ServiceLine serviceLine(List<String> platforms){
        return ServiceLine.builder()
                .platforms(platforms)
                .build();
    }

    public static EquipmentLine equipmentLine(List<String> platforms){
        return EquipmentLine.builder()
                .platforms(platforms)
                .build();
    }

}
