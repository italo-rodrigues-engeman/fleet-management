package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.EquipmentEntity;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentLine extends LinePPU {
    @JsonProperty("equipamentos")
    private List<EquipmentEntity> equipments;
    @JsonProperty("tipoEquipamento")
    private String equipmentType;
}
