package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.item.EquipmentDTO;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Representa um equipamento utilizado na operação.
 * Utilizada no módulo de PPU.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EquipmentEntity {
    @Id
    private String id;
    @JsonProperty("numeroPatrimonio")
    private String assetNumber;
    @JsonProperty("fabricante")
    private String manufacturer;
    @JsonProperty("modelo")
    private String model;
    @JsonProperty("plataforma")
    private String platform;


    public static EquipmentEntity fromDTO(EquipmentDTO dto) {
        return new EquipmentEntity(
                UUID.randomUUID().toString(),
                dto.numeroPatrimonio(),
                dto.fabricante(),
                dto.modelo(),
                dto.plataforma()
        );
    }
}
