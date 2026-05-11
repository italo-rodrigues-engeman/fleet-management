package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.item.EquipmentServiceDTO;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class RDOEquipment {
    @JsonProperty("id")
    private String id;
    @JsonProperty("numero")
    private String number;
    @JsonProperty  ("nome")
    private String name;
    @JsonProperty("checkers")
    private List<EquipmentChecker> checkers;
    @JsonProperty("totalPrevisto")
    private Integer totalPlanned;
    @JsonProperty("equipamentoId")
    private String equipmentPPUId;


    public static RDOEquipment fromDTO(EquipmentServiceDTO dto) {
    return RDOEquipment.builder()
            .id(dto.id())
            .number(dto.numero())
            .name(dto.nome())
            .checkers(
                    dto.equipamentosChecker() != null
                            ? dto.equipamentosChecker().stream()
                            .map(c -> new EquipmentChecker(
                                    c.id(),
                                    c.numeroPatrimonio(),
                                    c.fabricante(),
                                    c.descricaoModelo(),
                                    c.operacional() != null ? c.operacional() : Boolean.FALSE,
                                    c.observacao()
                            ))
                            .toList()
                            : List.of()
            )
            .equipmentPPUId(dto.equipamentoId())
            .build();
    }



}