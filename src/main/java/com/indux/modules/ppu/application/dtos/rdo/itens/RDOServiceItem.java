package com.indux.modules.ppu.application.dtos.rdo.itens;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Data
public class RDOServiceItem extends ServiceLine {
    @JsonProperty("presencaColaboradores")
    private List<RDOEmployees> employeesPresence;


    public static RDOServiceItem fromDTO(ServiceLine fromPPU, List<RDOEmployees> employeesPresenceInput) {
        if(fromPPU == null) {
            return new RDOServiceItem();
        };

        return RDOServiceItem.builder()
                .id(fromPPU.getId() != null ? fromPPU.getId() : UUID.randomUUID().toString())
                .genericNumber(fromPPU.getGenericNumber() != null ? fromPPU.getGenericNumber() : "")
                .name(fromPPU.getName() != null ? fromPPU.getName() : "")
                .unitOfMeasurement(fromPPU.getUnitOfMeasurement() != null ? fromPPU.getUnitOfMeasurement() : "")
                .value(fromPPU.getValue() != null ? fromPPU.getValue() : 0.0)
                .factor(fromPPU.getFactor() != null ? fromPPU.getFactor() : 0.0)
                .positions(fromPPU.getPositions() != null ? fromPPU.getPositions() : List.of())
                .employees(fromPPU.getEmployees() != null ? fromPPU.getEmployees() : List.of())
                .disposicao(fromPPU.getDisposicao() != null ? fromPPU.getDisposicao() : false)
                .employeesPresence(employeesPresenceInput != null ? employeesPresenceInput : List.of())
                .build();
    }
}
