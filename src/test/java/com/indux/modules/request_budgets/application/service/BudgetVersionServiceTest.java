package com.indux.modules.request_budgets.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.request_budgets.application.dto.BudgetVersionResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetVersionRequest;
import com.indux.modules.request_budgets.application.mapper.BudgetVersionMapper;
import com.indux.modules.request_budgets.domain.model.BudgetVersion;
import com.indux.modules.request_budgets.domain.repository.BudgetRepository;
import com.indux.modules.request_budgets.domain.repository.BudgetVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetVersionServiceTest {

    @Mock
    private BudgetVersionRepository budgetVersionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetVersionMapper budgetVersionMapper;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private BudgetVersionService budgetVersionService;

    private CreateBudgetVersionRequest createVersionRequest;
    private BudgetVersion budgetVersion;
    private String budgetId;
    private String versionId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = "budget-123";
        versionId = "version-456";

        createVersionRequest = CreateBudgetVersionRequest.builder()
                .tipoProposta("Tipo Teste")
                .dataEntrega(LocalDateTime.now().plusDays(30))
                .metodoEntrega("Entrega Teste")
                .manutencao("Manutenção Teste")
                .operacao("Operação Teste")
                .atividadesDiversas("Atividades Teste")
                .construcaoMontagem("Construção Teste")
                .fabricacao("Fabricação Teste")
                .projetos("Projetos Teste")
                .diversos("Diversos Teste")
                .detalhes("Detalhes Teste")
                .outros("Outros Teste")
                .comissao(new BigDecimal("5000.00"))
                .modalidadeConcorrencia("Modalidade Teste")
                .tipoOportunidade("tipo-123")
                .caracteristicasOportunidade("caracteristicas-123")
                .outroEmail("outro@test.com")
                .portal("Portal Teste")
                .acessoInfo("Acesso Info Teste")
                .mdInfo("MD Info Teste")
                .ppuInfo("PPU Info Teste")
                .smsInfo("SMS Info Teste")
                .geraisInfo("Gerais Info Teste")
                .habilitacaoInfo("Habilitação Info Teste")
                .anexoMd(new ArrayList<>())
                .anexoPpu(new ArrayList<>())
                .anexoSms(new ArrayList<>())
                .anexoGerais(new ArrayList<>())
                .habilitacaoAnexo(new ArrayList<>())
                .build();

        budgetVersion = BudgetVersion.builder()
                .id(versionId)
                .budgetId(budgetId)
                .tipoProposta("Tipo Teste")
                .dataEntrega(LocalDateTime.now().plusDays(30))
                .metodoEntrega("Entrega Teste")
                .manutencao("Manutenção Teste")
                .operacao("Operação Teste")
                .atividadesDiversas("Atividades Teste")
                .construcaoMontagem("Construção Teste")
                .fabricacao("Fabricação Teste")
                .projetos("Projetos Teste")
                .diversos("Diversos Teste")
                .detalhes("Detalhes Teste")
                .outros("Outros Teste")
                .comissao(new BigDecimal("5000.00"))
                .modalidadeConcorrencia("Modalidade Teste")
                .tipoOportunidade("tipo-123")
                .caracteristicasOportunidade("caracteristicas-123")
                .outroEmail("outro@test.com")
                .portal("Portal Teste")
                .acessoInfo("Acesso Info Teste")
                .mdInfo("MD Info Teste")
                .ppuInfo("PPU Info Teste")
                .smsInfo("SMS Info Teste")
                .geraisInfo("Gerais Info Teste")
                .habilitacaoInfo("Habilitação Info Teste")
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar versão de orçamento com sucesso")
    void shouldCreateBudgetVersionSuccessfully() {
        // Arrange
        when(budgetRepository.existsById(budgetId)).thenReturn(true);
        when(budgetVersionRepository.save(any(BudgetVersion.class))).thenReturn(budgetVersion);

        BudgetVersionResponseDTO responseDTO = BudgetVersionResponseDTO.builder()
                .id(versionId)
                .budgetId(budgetId)
                .tipoProposta("Tipo Teste")
                .build();

        when(budgetVersionMapper.toResponseDTO(budgetVersion)).thenReturn(responseDTO);

        // Act
        BudgetVersionResponseDTO result = budgetVersionService.createBudgetVersion(
                budgetId, createVersionRequest, userId.toString()
        );

        // Assert
        assertNotNull(result);
        assertEquals(versionId, result.getId());
        assertEquals(budgetId, result.getBudgetId());
        verify(budgetRepository, times(1)).existsById(budgetId);
        verify(budgetVersionRepository, times(1)).save(any(BudgetVersion.class));
        verify(budgetVersionMapper, times(1)).toResponseDTO(budgetVersion);
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não existe")
    void shouldThrowExceptionWhenBudgetNotFound() {
        // Arrange
        when(budgetRepository.existsById(budgetId)).thenReturn(false);

        // Act & Assert
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> budgetVersionService.createBudgetVersion(budgetId, createVersionRequest, userId.toString())
        );

        assertEquals("Orçamento não encontrado", exception.getMessage());
        verify(budgetRepository, times(1)).existsById(budgetId);
        verify(budgetVersionRepository, never()).save(any(BudgetVersion.class));
    }

    @Test
    @DisplayName("Deve buscar versão por ID com sucesso")
    void shouldGetBudgetVersionByIdSuccessfully() {
        // Arrange
        BudgetVersionResponseDTO responseDTO = BudgetVersionResponseDTO.builder()
                .id(versionId)
                .budgetId(budgetId)
                .tipoProposta("Tipo Teste")
                .build();

        when(budgetVersionRepository.findById(versionId)).thenReturn(Optional.of(budgetVersion));
        when(budgetVersionMapper.toResponseDTO(budgetVersion)).thenReturn(responseDTO);

        // Act
        BudgetVersionResponseDTO result = budgetVersionService.getBudgetVersionById(versionId);

        // Assert
        assertNotNull(result);
        assertEquals(versionId, result.getId());
        assertEquals(budgetId, result.getBudgetId());
        verify(budgetVersionRepository, times(1)).findById(versionId);
        verify(budgetVersionMapper, times(1)).toResponseDTO(budgetVersion);
    }

    @Test
    @DisplayName("Deve lançar exceção quando versão não encontrada")
    void shouldThrowExceptionWhenVersionNotFound() {
        // Arrange
        when(budgetVersionRepository.findById(versionId)).thenReturn(Optional.empty());

        // Act & Assert
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> budgetVersionService.getBudgetVersionById(versionId)
        );

        assertEquals("Versão do orçamento não encontrada", exception.getMessage());
        verify(budgetVersionRepository, times(1)).findById(versionId);
        verify(budgetVersionMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Deve buscar versões por budgetId com sucesso")
    void shouldGetBudgetVersionsByBudgetIdSuccessfully() {
        // Arrange
        List<BudgetVersion> versions = List.of(budgetVersion);
        BudgetVersionResponseDTO responseDTO = BudgetVersionResponseDTO.builder()
                .id(versionId)
                .budgetId(budgetId)
                .tipoProposta("Tipo Teste")
                .build();

        when(budgetVersionRepository.findByBudgetId(budgetId)).thenReturn(versions);
        when(budgetVersionMapper.toResponseDTO(budgetVersion)).thenReturn(responseDTO);

        // Act
        List<BudgetVersionResponseDTO> result = budgetVersionService.getBudgetVersionsByBudgetId(budgetId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(versionId, result.get(0).getId());
        assertEquals(budgetId, result.get(0).getBudgetId());
        verify(budgetVersionRepository, times(1)).findByBudgetId(budgetId);
        verify(budgetVersionMapper, times(1)).toResponseDTO(budgetVersion);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há versões")
    void shouldReturnEmptyListWhenNoVersions() {
        // Arrange
        when(budgetVersionRepository.findByBudgetId(budgetId)).thenReturn(new ArrayList<>());

        // Act
        List<BudgetVersionResponseDTO> result = budgetVersionService.getBudgetVersionsByBudgetId(budgetId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(budgetVersionRepository, times(1)).findByBudgetId(budgetId);
        verify(budgetVersionMapper, never()).toResponseDTO(any());
    }
}



