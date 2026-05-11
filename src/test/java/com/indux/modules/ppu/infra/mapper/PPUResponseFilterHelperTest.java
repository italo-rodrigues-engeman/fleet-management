package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.response.lines.AccessoryKitLineResponse;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.AccessoryKitLine;
import com.indux.modules.ppu.infra.mapper.ppu.LinePPUMapper;
import com.indux.modules.ppu.infra.mapper.response.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PPUResponseFilterHelperTest {

    @Test
    void shouldFilterOutInactiveAccessoryKits() {
        var activeKit = AccessoryKitLine.builder().id("kit-1").name("Kit ativo").active(true).build();
        var nullActiveKit = AccessoryKitLine.builder().id("kit-2").name("Kit sem flag").active(null).build();
        var inactiveKit = AccessoryKitLine.builder().id("kit-3").name("Kit inativo").active(false).build();

        var entity = PPUEntity.builder()
                .accessoryKits(List.of(activeKit, nullActiveKit, inactiveKit))
                .build();

        var response = new PPUResponse();

        var accessoryKitMapper = mock(AccessoryKitLineResponseMapper.class);
        when(accessoryKitMapper.toResponse(activeKit)).thenReturn(AccessoryKitLineResponse.builder().id("kit-1").build());
        when(accessoryKitMapper.toResponse(nullActiveKit)).thenReturn(AccessoryKitLineResponse.builder().id("kit-2").build());

        PPUResponseFilterHelper.applyFilters(
                response,
                entity,
                null,
                mock(ServiceLineResponseMapper.class),
                mock(EquipmentLineResponseMapper.class),
                mock(SteelCableLineResponseMapper.class),
                accessoryKitMapper,
                mock(CraneControlResponseMapper.class),
                mock(SteelCableControlResponseMapper.class),
                mock(EquipmentEntityResponseMapper.class),
                mock(LinePPUMapper.class)
        );

        assertEquals(2, response.getAccessoryKits().size());
        assertEquals(List.of("kit-1", "kit-2"), response.getAccessoryKits().stream().map(AccessoryKitLineResponse::getId).toList());
        verify(accessoryKitMapper, never()).toResponse(inactiveKit);
    }
}
