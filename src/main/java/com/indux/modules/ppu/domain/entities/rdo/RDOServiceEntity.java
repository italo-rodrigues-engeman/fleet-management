package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.application.dtos.item.ServiceItemDTO;
import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOEmployees;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Transient;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RDOServiceEntity {
    @JsonProperty("id")  private String id;
    @JsonProperty("idServico") private String serviceID;
    @JsonProperty("nomeServico") private String serviceName;
    @JsonProperty("numeroServico") private String serviceNumber;
    @JsonProperty("matricula") private String registration;
    @JsonProperty("nome") private String name;
    @JsonProperty("cargoId") private String cargoID;
    @JsonProperty("cargoNome") private String cargoNome;
    @JsonProperty("presenca") private boolean present;
    @JsonProperty("horario") private ShiftSchedule schedule;
    @JsonProperty("tipoDia") private String dayType;
    @JsonProperty("horaChegadaVoo") private LocalTime horaChegadaVoo;
    @JsonProperty("sinaleiro") private boolean flagman;
    @JsonProperty("horasExtras") private List<EmployeeOvertime> overtimes;
    @JsonProperty("sispat") private String sispat;
    @JsonProperty("caboTurma") private boolean teamLeader;
    @JsonProperty("adicionalNoturno") private Duration nightShiftPremium;
    @JsonProperty("horasNormais") private Duration normalHours;
    @JsonProperty("horasTotais") private Duration hourTotais;
    @JsonProperty("horasExtrasTotais") Duration overtimeHourTotais;
    @JsonProperty("statusColaborador") private String statusEmployee;
    @JsonProperty("novoStatusColaborador") private String newStatusEmployee;
    @Setter
    @JsonProperty("totalPrevisto") private Integer totalPlanned;
    @JsonProperty("disposicao") private Boolean disposicao;
    @JsonProperty("dobra") private Boolean doubleFold;
    @JsonProperty("valorMedido") private Double valueMeasured;
    @Transient
    @JsonIgnore private String mergeKey;
    @JsonProperty("tipoDisposicao") private String availableType;
    @JsonProperty("generated")
    private Boolean generated;

    public static RDOServiceEntity fromDTO(ServiceItemDTO service, RDOEmployees rdoDTO) {
        return RDOServiceEntity.builder()
                .id(UUID.randomUUID().toString())
                .serviceID(service.id())
                .registration(rdoDTO.matricula())
                .name(rdoDTO.nome())
                .cargoID(rdoDTO.cargoID())
                .cargoNome(rdoDTO.cargoNome())
                .present(true)
                .schedule(rdoDTO.horario())
                .dayType(rdoDTO.tipoDia())
                .horaChegadaVoo(rdoDTO.horaChegadaVoo())
                .flagman(Boolean.TRUE.equals(rdoDTO.sinaleiro()))
                .overtimes(normalizeOvertimes(rdoDTO.horasExtras()))
                .nightShiftPremium(rdoDTO.adicionalNoturno())
                .sispat(rdoDTO.sispat())
                .teamLeader(Boolean.TRUE.equals(rdoDTO.caboTurma()))
                .normalHours(rdoDTO.horasNormais())
                .hourTotais(rdoDTO.horasTotais())
                .overtimeHourTotais(rdoDTO.horasExtrasTotais())
                .statusEmployee(rdoDTO.statusColaborador())
                .serviceName(service.nome())
                .doubleFold(Boolean.TRUE.equals(rdoDTO.emDobra()))
                .serviceNumber(service.numero())
                .disposicao(service.disposicao() != null && service.disposicao())
                .availableType(rdoDTO.tipoDisposicao())
                .build();
    }

    private static List<EmployeeOvertime> normalizeOvertimes(List<EmployeeOvertime> overtimes) {
        if (overtimes == null || overtimes.isEmpty()) return List.of();
        
        var filtered = overtimes.stream()
                .filter(ot -> ot != null && ot.initial() != null && ot.end() != null)
                .toList();
        
        return filtered.isEmpty() ? List.of() : filtered;
    }
    public void updateFrom(RDOServiceEntity other) {
        this.schedule           = other.schedule;
        this.dayType            = other.dayType;
        this.horaChegadaVoo     = other.horaChegadaVoo;
        this.flagman            = other.flagman;
        this.overtimes          = other.overtimes;
        this.nightShiftPremium  = other.nightShiftPremium;
        this.normalHours        = other.normalHours;
        this.hourTotais         = other.hourTotais;
        this.overtimeHourTotais = other.overtimeHourTotais;
        this.statusEmployee     = other.statusEmployee;
    }

    public RDOServiceEntity copy() {
            RDOServiceEntity copy = new RDOServiceEntity();
            BeanUtils.copyProperties(this, copy);
            return copy;
    }

    public static RDOServiceEntity availableFromServiceLine(ServiceLine service, BoardedEmployee boardedEmployee) {
        return RDOServiceEntity.builder()
                .id(UUID.randomUUID().toString())
                .serviceID(service.getId())
                .serviceNumber(service.getGenericNumber())
                .serviceName(service.getName())
                .registration(boardedEmployee.getRegistration())
                .name(boardedEmployee.getName())
                .cargoID(boardedEmployee.getPosition())
                .cargoNome(boardedEmployee.getPositionName())
                .sispat(boardedEmployee.getSispat())
                .teamLeader(false)
                .present(true)
                .schedule(new ShiftSchedule())
                .dayType("rotina")
                .flagman(false)
                .overtimes(List.of())
                .nightShiftPremium(Duration.ZERO)
                .normalHours(Duration.ZERO)
                .hourTotais(Duration.ZERO)
                .overtimeHourTotais(Duration.ZERO)
                .statusEmployee("desconhecido")
                .doubleFold(false)
                .disposicao(true)
                .build();
    }

}
