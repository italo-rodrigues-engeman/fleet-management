package com.indux.modules.ppu.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.response.*;
import com.indux.modules.ppu.application.dtos.response.lines.*;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.item.PPUType;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PPUResponse {
    @JsonProperty("id")
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
    @JsonProperty("contractId")
    private Long contractId;
    @JsonProperty("plataformas")
    private List<String> platforms;
    @JsonProperty("frequenciasPlataformas")
    private Map<String, RDOFrequencyDTO> platformFrequencies;
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
    private List<ServiceLineResponse> services;
    @JsonProperty("linhas")
    private List<LinePPUResponse> lines;
    @JsonProperty("equipamentos")
    private List<EquipmentLineResponse> equipments;
    @JsonProperty("cabosAco")
    private List<SteelCableLineResponse> steelCables;
    @JsonProperty("totalPrevistoCabosAco")
    private List<MeasurementForecast> measurementForecastsSteelCables;
    @JsonProperty("kitAcessorios")
    private List<AccessoryKitLineResponse> accessoryKits;
    @JsonProperty("controleGuindastes")
    private List<CraneControlResponse> craneControls;
    @JsonProperty("controleCaboAco")
    private List<SteelCableControlResponse> steelCableControls;
    @JsonProperty("horarios")
    private List<ShiftScheduleResponse> shiftSchedule;
    @JsonProperty("caboDeTurma")
    private List<TeamLeaderResponse> teamLeader;
    @JsonProperty("periodoMedicao")
    private MeasurementPeriodResponse measurementPeriod;

    @JsonProperty("versao")
    private Long version;
    @JsonProperty("24h")
    private Boolean is24hType;
    @JsonProperty("apelido")
    private String nickname;
    @JsonProperty("valorSinaleiros")
    private Double signalmenValue;
    @JsonProperty("saldoTotal")
    private List<TotalBalanceResponse> totalBalances;
    @JsonProperty("projetoId")
    private Long projectId;
    @JsonProperty("temSupervisorBordo")
    private Boolean hasSupervisorOnBoard;
    @JsonProperty("permiteDuplicarRDO")
    private Boolean allowsRDODuplication;

    // Campos extras específicos do response
    @JsonProperty("usuario")
    private SimpleEmployeeDTO user;
    @JsonProperty("plataformaAtual")
    private String currentPlatform;
    @JsonProperty("colaboresEmbarcados")
    private List<BoardedEmployee> boardedEmployees; // colaboradores embarcados, mas não foram citados nos serviços
    @JsonProperty("colaboradoresEmDesembarque")
    private List<BoardedEmployee> inLandingDayEmployees;
    @JsonProperty("sequenciaRDO")
    private Long rdoCodeSequence;

}
