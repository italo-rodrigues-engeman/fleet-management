package com.indux.modules.ppu.domain.entities.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.domain.entities.item.*;
import com.indux.modules.ppu.domain.entities.ppu.*;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "ppu_collection")
@CompoundIndex(name = "contract_status_unique_idx", def = "{'contractId': 1, 'status': 1}", unique = true)
public class PPUEntity {

    @Id
    private String id;
    @JsonProperty("codigo")
    private Long codeID;
    @JsonProperty("regional")
    private Long regionalId;
    @JsonProperty("regional_nome")
    private String regionalNome;
    @JsonProperty("tipo")
    private PPUType type;
    @JsonProperty("responsavel")
    private String responsible;
    @JsonProperty("dataPPU")
    private DateRange dateRange;

    @JsonProperty("contrato")
    private Map<String, Object> contract;
    // TODO: Remover tb_contratos
    @JsonProperty("contractId")
    private Long contractId; // id do kogni, tb_contratos.
    @JsonProperty("plataformas")
    private List<String> platforms;
    @JsonProperty("frequenciasPlataformas")
    private Map<String, RDOFrequency> platformFrequencies;
    @JsonProperty("cliente")
    private Long clientId;
    @JsonProperty("observacoes_gerais")
    private String generalObservation;
    @JsonProperty("status")
    private DocumentStatus status;
    @JsonProperty("qtdSinaleiros")
    private Integer signalmenQuantity;
    @JsonProperty("planilhaSAMC")
    private Boolean mandatorySAMC;
    @JsonProperty("auditableConfig")
    private AuditableConfig auditableConfig;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

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
    @JsonProperty("horarios")
    private List<ShiftSchedule> shiftSchedule;
    @JsonProperty("caboDeTurma")
    private List<TeamLeader> teamLeader;
    @JsonProperty("periodoMedicao")
    private MeasurementPeriod measurementPeriod;
    @JsonProperty("linhas")
    private List<LinePPU> lines;

    @JsonProperty("versao")
    private Long  version;
    @JsonProperty("24h")
    private Boolean is24hType;
    @JsonProperty("apelido")
    private String nickname;
    @JsonProperty("valorSinaleiros")
    private Double signalmenValue;
    @JsonProperty("saldoTotal")
    private List<TotalBalance> totalBalances;
    @JsonProperty("projetoId")
    private Long projectId;
    @JsonProperty("tipoDisposicao")
    @Nullable
    private List<AvailableType> availableType;
    @JsonProperty("temSupervisorBordo")
    private Boolean hasSupervisorOnBoard;
    @JsonProperty("permiteDuplicarRDO")
    private Boolean allowsRDODuplication;

    public boolean hasAvailableType() {
        return availableType != null && !availableType.isEmpty();
    }
}
