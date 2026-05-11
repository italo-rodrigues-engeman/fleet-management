package com.indux.modules.ppu.application.dtos.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RDOResponse extends RDOEntity {

    @JsonProperty("servicosNaoUsados")
    private List<String> unusedServices;
    @JsonProperty("horariosPPU")
    private List<ShiftSchedule> shiftSchedules;
    @JsonProperty("obrigadoSAMC")
    private Boolean mandatorySAMC;
    @JsonProperty("ppuAutorizaDuplicacao")
    private Boolean ppuAllowsDuplication;

    public static RDOResponse fromEntity(
            RDOEntity entity,
            List<String> unusedServices,
            List<ShiftSchedule> shifts,
            boolean mandatorySAMC,
            boolean ppuAllowsDuplication
    ) {
        RDOResponse response = new RDOResponse();
        BeanUtils.copyProperties(entity, response);
        response.setUnusedServices(unusedServices);
        response.setShiftSchedules(shifts);
        response.setMandatorySAMC(mandatorySAMC);
        response.setPpuAllowsDuplication(ppuAllowsDuplication);
        return response;
    }

}
