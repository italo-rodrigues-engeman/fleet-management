package com.indux.modules.ppu.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import com.indux.modules.ppu.domain.entities.item.TeamLeader;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.AccessoryKitLine;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.ppu.SteelCableLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PPUWithClientNameDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("codigo")
    private Long codeID;
    @JsonProperty("filial")
    private Long branch;
    @JsonProperty("filial_nome")
    private String branchName;
    @JsonProperty("contrato")
    private Map<String, Object> contract;
    @JsonProperty("plataformas")
    private List<String> platforms;
    @JsonProperty("cliente")
    private Long clientId;
    @JsonProperty("clienteNome")
    private String clienteNome;
    @JsonProperty("servicos")
    private List<ServiceLine> services;
    @JsonProperty("equipamentos")
    private List<EquipmentLine> equipments;
    @JsonProperty("cabosAco")
    private List<SteelCableLine> steelCables;
    @JsonProperty("kitAcessorios")
    private List<AccessoryKitLine> accessoryKits;
    @JsonProperty("controleGuindastes")
    private List<CraneControl> craneControls;
    @JsonProperty("controleCaboAco")
    private List<SteelCableControl> steelCableControls;
    @JsonProperty("observacoes_gerais")
    private String generalObservation;
    @JsonProperty("horarios")
    private List<ShiftSchedule> shiftSchedule;
    @JsonProperty("status")
    private DocumentStatus status;
    @JsonProperty("qtdSinaleiros")
    private Integer signalmenQuantity;
    @JsonProperty("caboDeTurma")
    private List<TeamLeader> teamLeader;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static PPUWithClientNameDTO fromPPUEntity(PPUEntity ppu, String clienteNome) {
        return new PPUWithClientNameDTO(
                ppu.getId(),
                ppu.getCodeID(),
                ppu.getRegionalId(),
                ppu.getRegionalNome(),
                ppu.getContract(),
                ppu.getPlatforms(),
                ppu.getClientId(),
                clienteNome,
                ppu.getServices(),
                ppu.getEquipments(),
                ppu.getSteelCables(),
                ppu.getAccessoryKits(),
                ppu.getCraneControls(),
                ppu.getSteelCableControls(),
                ppu.getGeneralObservation(),
                ppu.getShiftSchedule(),
                ppu.getStatus(),
                ppu.getSignalmenQuantity(),
                ppu.getTeamLeader(),
                ppu.getCreatedBy(),
                ppu.getCreatedAt(),
                ppu.getUpdatedBy(),
                ppu.getUpdatedAt()
        );
    }
} 