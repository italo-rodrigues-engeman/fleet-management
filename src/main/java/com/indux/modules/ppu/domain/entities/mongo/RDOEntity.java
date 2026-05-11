package com.indux.modules.ppu.domain.entities.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOEmployees;
import com.indux.modules.ppu.application.dtos.rdo.RDORecord;
import com.indux.modules.ppu.application.dtos.requests.DivergenceRecord;
import com.indux.modules.ppu.application.dtos.requests.RDOLineRequest;
import com.indux.modules.ppu.domain.entities.rdo.SteelCableChecker;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.mongodb.lang.Nullable;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "ppu_rdos")
@Builder
public class RDOEntity {
    @Id private String id;
    @JsonProperty("ppuId") private String ppuId;
    @JsonProperty("plataforma") private String platform;
    @JsonProperty("idSequencial") private Long sequentialId;
    @JsonProperty("data") private LocalDate date;
    @JsonProperty("matricula") private String creatorRegistration;
    @JsonProperty("encarregado") private String creatorName;
    @CreatedDate private LocalDateTime createdAt;
    @JsonProperty("criadoVia") private String createdBy;
    @JsonProperty("cargo") private String creatorPosition;
    @JsonProperty("contrato") private Map<String, Object> contract;
    @JsonProperty("cliente") private String clientName;
    @JsonProperty("regional") private String regionalNome;
    @JsonProperty("competencia") private String competence;


    // -- gerenciar -- //
    @JsonProperty("statusDP")
    private RDOStatusDP statusDP;
    @JsonProperty("statusOP")
    private RDOStatusOP statusOP;

    // -- dados cliente -- //
    @JsonProperty("dadosCliente")
    private @Nullable ClientEmployee clientEmployee;
    @JsonProperty("observacoesContratada")
    private String contractorObservations;

    // -- dados PPU -- //
    @JsonProperty("equipamentos")
    private List<RDOEquipment> equipments;
    @JsonProperty("totalPrevistoEquipamentos")
    private Integer totalPlannedEquipments;
    @JsonProperty("cabosDeAco")
    private List<SteelCableChecker> steelCable;
    @JsonProperty("kitsAcessorios")
    private List<AccessoryKitDTO> accessoryKits;
    @JsonProperty("controleGuindaste")
    private List<CraneControl> craneControl;
    @JsonProperty("controleCaboAco")
    private List<SteelCableControl> cableControl;
    @JsonProperty("trabalhoDesembarque")
    private List<RDOEmployeeDeparture> employeeDepartures;
    @JsonProperty("servicos")
    private List<RDOServiceEntity> services;
    @JsonProperty("servicosContratada")
    private String contractorServices;
    @JsonProperty("justificativaHoraExtra")
    private String overtimeJustification;
    @JsonProperty("linhas")
    private List<RDOLine> lines;

    // -- dados de aprovação -- //
    @JsonProperty("anexos") private List<String> attachments;
    @JsonProperty("historico") private List<RDOLogger> loggers;
    @JsonProperty("divergencias") private List<DivergenceRecord> divergences;

    @JsonProperty("projetoId") private Long projectId;
    @JsonProperty("projetoNome") private String projectName;
    @JsonProperty("regionalId") private Long regionalId;
    @JsonProperty("duplicado") private Boolean duplicated;




    public static RDOEntity fromDTOCreate(RDORecord record, Long codeID, EmployeeDTO registration, PPUEntity ppu, String clientName){
        RDOStatusOP initialStatusOP = Boolean.FALSE.equals(ppu.getHasSupervisorOnBoard())
                ? RDOStatusOP.APPROVED
                : RDOStatusOP.PENDING;

        return RDOEntity.builder()
                .ppuId(record.PPUid())
                .platform(record.plataforma())
                .sequentialId(codeID)
                .date(record.data().atStartOfDay(ZoneOffset.UTC).toLocalDate())
                .clientName(clientName)
                .contract(ppu.getContract())
                .equipments(record.equipamentos().stream()
                        .map(RDOEquipment::fromDTO)
                        .collect(Collectors.toList()))
                .regionalNome(ppu.getRegionalNome())
                .regionalId(ppu.getRegionalId())
                .projectId(ppu.getProjectId())
                .creatorRegistration(registration.getMatricula())
                .creatorName(registration.getName())
                .statusDP(RDOStatusDP.PENDING)
                .statusOP(initialStatusOP)
                .clientEmployee(record.dadosCliente())
                .craneControl(record.controleGuindaste().stream().map(CraneControl::fromDTO).toList())
                .cableControl(record.controleCaboAco().stream().map(SteelCableControl::fromDTO)
                        .toList())
                .steelCable(record.cabosDeAcos())
                .accessoryKits(record.kitsAcessorios())
                .contractorServices(record.servicosRealizadosContratada())
                .contractorObservations(record.registrosObservacao())
                .services(record.servicos().stream()
                        .flatMap(serviceItemDTO -> {
                            List<RDOEmployees> presencas = serviceItemDTO
                                    .presencaColaboradores();
                            if (presencas == null || presencas.isEmpty())
                                return Stream.empty();
                            return presencas.stream()
                                    .map(rdoEmployee -> RDOServiceEntity.fromDTO(
                                            serviceItemDTO, rdoEmployee));
                        })
                        .collect(Collectors.toList()))
                .employeeDepartures(record.trabalhoDesembarque().stream()
                        .map(RDOEmployeeDeparture::fromDTO).toList())
                .createdBy(record.platformService())
                .competence("-")
                .overtimeJustification(record.justificativaHoraExtra())
                .lines(record.linhasRDO() != null
                        ? record.linhasRDO().stream().map(RDOLineRequest::toEntity).toList()
                        : List.of())
                .duplicated(Boolean.TRUE.equals(record.duplicado()))
                .build();
    }

    public static RDOEntity createBasic(PPUEntity ppu, String platform, LocalDate date, String createdBy,
                                        String creatorRegistration, String creatorName) {
        RDOStatusOP initialStatusOP = Boolean.FALSE.equals(ppu.getHasSupervisorOnBoard())
                ? RDOStatusOP.APPROVED
                : RDOStatusOP.PENDING;
        return RDOEntity.builder()
                .ppuId(ppu.getId())
                .regionalNome(ppu.getRegionalNome())
                .projectId(ppu.getProjectId())
                .contract(ppu.getContract())
                .platform(platform)
                .date(date)
                .createdBy(createdBy)
                .creatorRegistration(creatorRegistration)
                .creatorName(creatorName)
                .statusDP(RDOStatusDP.PENDING)
                .statusOP(initialStatusOP)
                .competence("-")
                .equipments(new ArrayList<>())
                .steelCable(new ArrayList<>())
                .accessoryKits(new ArrayList<>())
                .craneControl(new ArrayList<>())
                .cableControl(new ArrayList<>())
                .employeeDepartures(new ArrayList<>())
                .services(new ArrayList<>())
                .lines(new ArrayList<>())
                .attachments(new ArrayList<>())
                .loggers(new ArrayList<>())
                .divergences(new ArrayList<>())
                .duplicated(false)
                .build();
    }

}
