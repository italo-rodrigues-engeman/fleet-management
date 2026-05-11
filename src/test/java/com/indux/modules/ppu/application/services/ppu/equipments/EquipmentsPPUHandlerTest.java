package com.indux.modules.ppu.application.services.ppu.equipments;

import com.indux.modules.ppu.application.services.fixtures.PPUFixture;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.response.EquipmentLineResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentsPPUHandler Tests")
class EquipmentsPPUHandlerTest {

    @Mock
    private PPURepository ppuRepository;
    @Spy
    private EquipmentLineResponseMapper mapper;

    @InjectMocks
    private EquipmentsPPUHandler equipmentsPPUHandler;

    private final String FIXED_PPU_ID = "6849cf01c7b0250cfab80edc";
    private PPUEntity mockPPU;

    @BeforeEach
    void setUp() {
        var equipment1 = EquipmentLine.builder().id("eq-1").name("Equipment 1").build();
        var equipment2 = EquipmentLine.builder().id("eq-2").name("Equipment 2").build();

        mockPPU = PPUEntity.builder()
                .id(FIXED_PPU_ID)
                .equipments(List.of(equipment1, equipment2))
                .platforms(List.of("P-74", "P-75"))
                .build();
    }

    @Nested
    @DisplayName("fetchEquipments Method")
    class FetchEquipmentsTests {

        @Test
        @DisplayName("Should throw RuntimeException when PPU is not found")
        void fetchEquipments_shouldThrowException_whenPPUNotFound() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.empty());

            var exception = assertThrows(RuntimeException.class, () -> equipmentsPPUHandler.fetchEquipments(FIXED_PPU_ID));
            assertThat(exception.getMessage()).isEqualTo("PPU não encontrada no sistema.");
            verify(ppuRepository).findById(FIXED_PPU_ID);
        }
    }

    @Nested
    @DisplayName("updateEquipments Method")
    class UpdateEquipmentsTests {

        @Test
        @DisplayName("Should update equipments and save PPU when data is valid")
        //todo: corrigir o erro do teste relacionado a mudança de EquipmentServiceLine para EquipmentService
        void updateEquipments_shouldUpdateAndSave_whenDataIsValid() {
            var newEquipmentDTO = EquipmentLine.builder().name("New Equipment").build();
            var equipmentDTOs = List.of(newEquipmentDTO);

            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            equipmentsPPUHandler.updateEquipments(equipmentDTOs, FIXED_PPU_ID);

            ArgumentCaptor<PPUEntity> ppuCaptor = ArgumentCaptor.forClass(PPUEntity.class);
            verify(ppuRepository).save(ppuCaptor.capture());

            PPUEntity savedPPU = ppuCaptor.getValue();
            assertThat(savedPPU.getEquipments()).hasSize(1);
            assertThat(savedPPU.getEquipments().getFirst().getName()).isEqualTo("New Equipment");
        }

        @Test
        @DisplayName("Should throw RuntimeException when PPU is not found")
        void updateEquipments_shouldThrowException_whenPPUNotFound() {
            var equipmentDTOs = List.of(PPUFixture.fakeEquipmentService);
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.empty());

            var exception = assertThrows(RuntimeException.class, () -> equipmentsPPUHandler.updateEquipments(equipmentDTOs, FIXED_PPU_ID));
            assertThat(exception.getMessage()).isEqualTo("PPU não encontrada no sistema.");
            verify(ppuRepository, never()).save(any(PPUEntity.class));
        }
    }
}