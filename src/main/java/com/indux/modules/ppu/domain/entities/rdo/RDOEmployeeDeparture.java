package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOEmployees;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RDOEmployeeDeparture {

    @JsonProperty("matricula")
    private String registration;
    @JsonProperty("nome")
    private String name;
    @JsonProperty("cargoID")
    private String positionID;
    @JsonProperty("cargoNome")
    private String positionName;
    @JsonProperty("horaSaidaVoo")
    private LocalTime departureHour;

    public static RDOEmployeeDeparture fromDTO(RDOEmployees record){
        return new RDOEmployeeDeparture(
                record.matricula(),
                record.nome(),
                record.cargoID(),
                record.cargoNome(),
                record.horaSaidaVoo()
        );
    }
}
