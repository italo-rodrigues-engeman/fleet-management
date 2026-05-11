package com.indux.modules.ppu.presentation.controller;

import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.response.RDOUpdaterResponse;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.application.services.rdo.operation.DuplicateRDOUseCase;
import com.indux.modules.ppu.application.services.rdo.operation.audit.GenerateAuditExcelService;
import com.indux.modules.ppu.application.services.rdo.rh.FetchUpdaterRDO;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.services.bm.audit.samc.AuditSAMC;
import com.indux.modules.ppu.domain.entities.item.PPUType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RDOCoordinatorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RDOService service;

    @Mock
    private FetchUpdaterRDO fetchUpdater;

    @Mock
    private DuplicateRDOUseCase duplicateUseCase;

    @Mock
    private AuditSAMC audit;

    @Mock
    private GenerateAuditExcelService generateExcelService;

    @InjectMocks
    private RDOCoordinatorController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /fetch/update/{id} deve retornar RDOUpdaterResponse com PPUResponse contendo campos JSON corretos")
    void fetchUpdate_ShouldReturnCorrectJsonFields() throws Exception {
        // Arrange
        String rdoId = "rdo-123";
        RDOEntity rdo = createMockRDO();
        PPUResponse ppuResponse = createMockPPUResponse();
        RDOUpdaterResponse response = new RDOUpdaterResponse(rdo, ppuResponse);

        when(fetchUpdater.execute(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/solicitacoes/ppu/rdo/op/fetch/update/{id}", rdoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificar estrutura do RDOUpdaterResponse
                .andExpect(jsonPath("$.rdo").exists())
                .andExpect(jsonPath("$.ppu").exists())
                // Verificar campos do PPUResponse dentro do RDOUpdaterResponse
                .andExpect(jsonPath("$.ppu.id").value("ppu-123"))
                .andExpect(jsonPath("$.ppu.codigo").value(12345L))
                .andExpect(jsonPath("$.ppu.regional").value(1L))
                .andExpect(jsonPath("$.ppu.regional_nome").value("Regional Test"))
                .andExpect(jsonPath("$.ppu.tipo").value("MOVIMENTO_CARGAS"))
                .andExpect(jsonPath("$.ppu.responsavel").value("Responsável Test"))
                .andExpect(jsonPath("$.ppu.contrato").exists())
                .andExpect(jsonPath("$.ppu.contractId").value(100L))
                .andExpect(jsonPath("$.ppu.plataformas").isArray())
                .andExpect(jsonPath("$.ppu.cliente").value(200L))
                .andExpect(jsonPath("$.ppu.observacoes_gerais").value("Observação geral"))
                .andExpect(jsonPath("$.ppu.status").value("ABERTO"))
                .andExpect(jsonPath("$.ppu.qtdSinaleiros").value(5))
                .andExpect(jsonPath("$.ppu.planilhaSAMC").value(true))
                .andExpect(jsonPath("$.ppu.created_by").value("user1"))
                .andExpect(jsonPath("$.ppu.created_at").exists())
                .andExpect(jsonPath("$.ppu.updated_by").value("user2"))
                .andExpect(jsonPath("$.ppu.updated_at").exists())
                // Verificar campos de listas do PPUResponse
                .andExpect(jsonPath("$.ppu.servicos").isArray())
                .andExpect(jsonPath("$.ppu.equipamentos").isArray())
                .andExpect(jsonPath("$.ppu.cabosAco").isArray())
                .andExpect(jsonPath("$.ppu.totalPrevistoCabosAco").isArray())
                .andExpect(jsonPath("$.ppu.kitAcessorios").isArray())
                .andExpect(jsonPath("$.ppu.controleGuindastes").isArray())
                .andExpect(jsonPath("$.ppu.controleCaboAco").isArray())
                .andExpect(jsonPath("$.ppu.horarios").isArray())
                .andExpect(jsonPath("$.ppu.caboDeTurma").isArray())
                .andExpect(jsonPath("$.ppu.saldoTotal").isArray())
                .andExpect(jsonPath("$.ppu.versao").value(1L))
                .andExpect(jsonPath("$.ppu.['24h']").value(false))
                .andExpect(jsonPath("$.ppu.apelido").value("PPU Test"))
                .andExpect(jsonPath("$.ppu.valorSinaleiros").value(1000.0))
                .andExpect(jsonPath("$.ppu.projetoId").value(300L))
                .andExpect(jsonPath("$.ppu.usuario").exists())
                .andExpect(jsonPath("$.ppu.plataformaAtual").value("P-01"))
                .andExpect(jsonPath("$.ppu.colaboresEmbarcados").isArray())
                .andExpect(jsonPath("$.ppu.colaboradoresEmDesembarque").isArray())
                .andExpect(jsonPath("$.ppu.sequenciaRDO").value(10L));
    }

    @Test
    @DisplayName("POST /audit/multiple deve delegar para auditMultipleRDOs e retornar 200")
    void auditMultiple_ShouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "samc.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-content".getBytes()
        );

        when(audit.auditMultipleRDOs(anyList(), any(), any())).thenReturn(List.of());

        mockMvc.perform(multipart("/api/solicitacoes/ppu/rdo/op/audit/multiple")
                        .file(file)
                        .param("rdos", "rdo-1", "rdo-2")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(audit).auditMultipleRDOs(anyList(), any(), any());
    }

    @Test
    @DisplayName("GET /fetch/by-ppu-and-date deve retornar vários RDOs para a mesma data e PPU")
    void fetchByPpuAndDate_ShouldReturnRDOList() throws Exception {
        RDOEntity firstRdo = new RDOEntity();
        firstRdo.setId("rdo-1");
        firstRdo.setPpuId("ppu-123");
        firstRdo.setPlatform("P-01");
        firstRdo.setDate(LocalDate.of(2026, 4, 10));

        RDOEntity secondRdo = new RDOEntity();
        secondRdo.setId("rdo-2");
        secondRdo.setPpuId("ppu-123");
        secondRdo.setPlatform("P-02");
        secondRdo.setDate(LocalDate.of(2026, 4, 10));

        when(service.fetchAllByPpuAndDate(anyString(), any())).thenReturn(List.of(firstRdo, secondRdo));

        mockMvc.perform(get("/api/solicitacoes/ppu/rdo/op/fetch/by-ppu-and-date")
                        .param("ppuId", "ppu-123")
                        .param("date", "2026-04-10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("rdo-1"))
                .andExpect(jsonPath("$[0].plataforma").value("P-01"))
                .andExpect(jsonPath("$[1].id").value("rdo-2"))
                .andExpect(jsonPath("$[1].plataforma").value("P-02"));
    }

    @Test
    @DisplayName("POST /approve/batch-by-ppu-and-date deve aprovar lote e retornar os RDOs")
    void approveBatchByPpuAndDate_ShouldReturnApprovedList() throws Exception {
        RDOEntity rdo = new RDOEntity();
        rdo.setId("rdo-1");
        rdo.setPpuId("ppu-123");
        rdo.setPlatform("P-01");

        when(service.approveBatchByPpuAndDate(anyString(), any(), any())).thenReturn(List.of(rdo));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/api/solicitacoes/ppu/rdo/op/approve/batch-by-ppu-and-date")
                        .param("ppuId", "ppu-123")
                        .param("date", "2026-04-10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("rdo-1"))
                .andExpect(jsonPath("$[0].ppuId").value("ppu-123"));
    }

    private RDOEntity createMockRDO() {
        RDOEntity rdo = new RDOEntity();
        rdo.setId("rdo-123");
        rdo.setPpuId("ppu-123");
        rdo.setPlatform("P-01");
        return rdo;
    }

    private PPUResponse createMockPPUResponse() {
        PPUResponse response = new PPUResponse();
        response.setId("ppu-123");
        response.setCodeID(12345L);
        response.setRegionalId(1L);
        response.setRegionalNome("Regional Test");
        response.setType(PPUType.MOVIMENTO_CARGAS);
        response.setResponsible("Responsável Test");
        response.setDateRange(new DateRange(LocalDate.now(), LocalDate.now().plusDays(30)));
        response.setContract(Map.of("id", 100L, "nome", "Contrato Test"));
        response.setContractId(100L);
        response.setPlatforms(List.of("P-01", "P-02"));
        response.setClientId(200L);
        response.setGeneralObservation("Observação geral");
        response.setStatus(DocumentStatus.ABERTO);
        response.setSignalmenQuantity(5);
        response.setMandatorySAMC(true);
        response.setCreatedBy("user1");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedBy("user2");
        response.setUpdatedAt(LocalDateTime.now());
        response.setServices(List.of());
        response.setEquipments(List.of());
        response.setSteelCables(List.of());
        response.setMeasurementForecastsSteelCables(List.of());
        response.setAccessoryKits(List.of());
        response.setCraneControls(List.of());
        response.setSteelCableControls(List.of());
        response.setShiftSchedule(List.of());
        response.setTeamLeader(List.of());
        response.setMeasurementPeriod(null);
        response.setVersion(1L);
        response.setIs24hType(false);
        response.setNickname("PPU Test");
        response.setSignalmenValue(1000.0);
        response.setTotalBalances(List.of());
        response.setProjectId(300L);
        response.setUser(new SimpleEmployeeDTO("12345", "User Test", "Cargo_ID", "Cargo_Nome", "SISPAT123", Map.of()));
        response.setCurrentPlatform("P-01");
        response.setBoardedEmployees(List.of());
        response.setInLandingDayEmployees(List.of());
        response.setRdoCodeSequence(10L);
        return response;
    }
}
