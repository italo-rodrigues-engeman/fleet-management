package com.indux.modules.ppu.application.dtos.item;

import jakarta.validation.constraints.NotNull;

public record EquipmentDTO(
        @NotNull(message = "O número do patrimônio deve ser preenchido.", groups = EquipmentServiceDTO.EquipmentCreate.class) String numeroPatrimonio,
        @NotNull(message = "O fabricante deve ser preenchido.", groups = EquipmentServiceDTO.EquipmentCreate.class) String fabricante,
        @NotNull(message = "O modelo deve ser preenchido.", groups = EquipmentServiceDTO.EquipmentCreate.class) String modelo,
        @NotNull(message = "A plataforma deve ser preenchido.", groups = EquipmentServiceDTO.EquipmentCreate.class) String plataforma
) {
}
