package com.indux.modules.request_budgets.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.request_budgets.application.dto.BudgetResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetResponseDTO;
import com.indux.modules.request_budgets.application.mapper.BudgetMapper;
import com.indux.modules.request_budgets.domain.model.Budget;
import com.indux.modules.request_budgets.domain.model.BudgetDocumento;
import com.indux.modules.request_budgets.domain.model.BudgetSolicitante;
import com.indux.modules.request_budgets.domain.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private CreateBudgetRequest createBudgetRequest;
    private Budget budget;
    private UUID userId;
    private String budgetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = "budget-123";

        createBudgetRequest = CreateBudgetRequest.builder()
                .clienteId(1L)
                .mercadoId(2L)
                .setorId(3L)
                .nomeOportunidade("Oportunidade Teste")
                .representanteComercialId(UUID.randomUUID())
                .solicitanteNome("João Silva")
                .solicitanteEmail1("joao@test.com")
                .descricaoOportunidade("Descrição teste")
                .tempoContrato("12 meses")
                .porteEstimado(new BigDecimal("100000.00"))
                .dataAbertura(LocalDate.now())
                .detalhesGerais("Detalhes gerais")
                .status("PENDENTE")
                .dataAcompanhamento(LocalDateTime.now())
                .solicitantes(new ArrayList<>())
                .documentos(new ArrayList<>())
                .build();

        budget = Budget.builder()
                .id(budgetId)
                .clienteId(1L)
                .mercadoId(2L)
                .setorId(3L)
                .nomeOportunidade("Oportunidade Teste")
                .representanteComercialId(UUID.randomUUID())
                .solicitanteNome("João Silva")
                .solicitanteEmail1("joao@test.com")
                .descricaoOportunidade("Descrição teste")
                .tempoContrato("12 meses")
                .porteEstimado(new BigDecimal("100000.00"))
                .dataAbertura(LocalDate.now())
                .detalhesGerais("Detalhes gerais")
                .status("PENDENTE")
                .dataAcompanhamento(LocalDateTime.now())
                .solicitantes(new ArrayList<>())
                .documentos(new ArrayList<>())
                .stepLog(new ArrayList<>())
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar orçamento com sucesso")
    void shouldCreateBudgetSuccessfully() {
        // Arrange
        when(budgetRepository.existsByNomeOportunidade(createBudgetRequest.getNomeOportunidade()))
                .thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        // Act
        CreateBudgetResponseDTO result = budgetService.createBudget(createBudgetRequest, userId.toString());

        // Assert
        assertNotNull(result);
        assertEquals("Orçamento criado com sucesso", result.message());
        assertEquals(budgetId, result.budgetId());
        assertEquals("Oportunidade Teste", result.nomeOportunidade());
        verify(budgetRepository, times(1)).existsByNomeOportunidade(createBudgetRequest.getNomeOportunidade());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome de oportunidade já existe")
    void shouldThrowExceptionWhenNomeOportunidadeExists() {
        // Arrange
        when(budgetRepository.existsByNomeOportunidade(createBudgetRequest.getNomeOportunidade()))
                .thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.createBudget(createBudgetRequest, userId.toString())
        );

        assertEquals("Já existe um orçamento com este nome de oportunidade", exception.getMessage());
        verify(budgetRepository, times(1)).existsByNomeOportunidade(createBudgetRequest.getNomeOportunidade());
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve criar orçamento com solicitantes")
    void shouldCreateBudgetWithSolicitantes() {
        // Arrange
        CreateBudgetRequest requestWithSolicitantes = CreateBudgetRequest.builder()
                .clienteId(1L)
                .mercadoId(2L)
                .setorId(3L)
                .nomeOportunidade("Oportunidade com Solicitantes")
                .representanteComercialId(UUID.randomUUID())
                .solicitantes(List.of(
                        com.indux.modules.request_budgets.application.dto.BudgetSolicitanteDTO.builder()
                                .nome("Maria Silva")
                                .email1("maria@test.com")
                                .build()
                ))
                .build();

        BudgetSolicitante solicitanteEntity = BudgetSolicitante.builder()
                .nome("Maria Silva")
                .email1("maria@test.com")
                .build();

        when(budgetRepository.existsByNomeOportunidade(requestWithSolicitantes.getNomeOportunidade()))
                .thenReturn(false);
        when(budgetMapper.toSolicitanteEntity(any())).thenReturn(solicitanteEntity);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        // Act
        CreateBudgetResponseDTO result = budgetService.createBudget(requestWithSolicitantes, userId.toString());

        // Assert
        assertNotNull(result);
        verify(budgetMapper, times(1)).toSolicitanteEntity(any());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve criar orçamento com documentos")
    void shouldCreateBudgetWithDocumentos() {
        // Arrange
        CreateBudgetRequest requestWithDocumentos = CreateBudgetRequest.builder()
                .clienteId(1L)
                .mercadoId(2L)
                .setorId(3L)
                .nomeOportunidade("Oportunidade com Documentos")
                .representanteComercialId(UUID.randomUUID())
                .documentos(List.of(
                        com.indux.modules.request_budgets.application.dto.BudgetDocumentoDTO.builder()
                                .tipo("PDF")
                                .descricao("Documento teste")
                                .build()
                ))
                .build();

        BudgetDocumento documentoEntity = BudgetDocumento.builder()
                .tipo("PDF")
                .descricao("Documento teste")
                .build();

        when(budgetRepository.existsByNomeOportunidade(requestWithDocumentos.getNomeOportunidade()))
                .thenReturn(false);
        when(budgetMapper.toDocumentoEntity(any())).thenReturn(documentoEntity);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        // Act
        CreateBudgetResponseDTO result = budgetService.createBudget(requestWithDocumentos, userId.toString());

        // Assert
        assertNotNull(result);
        verify(budgetMapper, times(1)).toDocumentoEntity(any());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID com sucesso")
    void shouldGetBudgetByIdSuccessfully() {
        // Arrange
        BudgetResponseDTO responseDTO = BudgetResponseDTO.builder()
                .id(budgetId)
                .nomeOportunidade("Oportunidade Teste")
                .build();

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetMapper.toResponseDTO(budget)).thenReturn(responseDTO);

        // Act
        BudgetResponseDTO result = budgetService.getBudgetById(budgetId);

        // Assert
        assertNotNull(result);
        assertEquals(budgetId, result.getId());
        assertEquals("Oportunidade Teste", result.getNomeOportunidade());
        verify(budgetRepository, times(1)).findById(budgetId);
        verify(budgetMapper, times(1)).toResponseDTO(budget);
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não encontrado")
    void shouldThrowExceptionWhenBudgetNotFound() {
        // Arrange
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // Act & Assert
        ModuleNotFoundFailure exception = assertThrows(
                ModuleNotFoundFailure.class,
                () -> budgetService.getBudgetById(budgetId)
        );

        assertEquals("Orçamento não encontrado", exception.getMessage());
        verify(budgetRepository, times(1)).findById(budgetId);
        verify(budgetMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Deve retornar todos os orçamentos")
    void shouldGetAllBudgets() {
        // Arrange
        List<Budget> budgets = List.of(budget);
        BudgetResponseDTO responseDTO = BudgetResponseDTO.builder()
                .id(budgetId)
                .nomeOportunidade("Oportunidade Teste")
                .build();

        when(budgetRepository.findAll()).thenReturn(budgets);
        when(budgetMapper.toResponseDTO(budget)).thenReturn(responseDTO);

        // Act
        List<BudgetResponseDTO> result = budgetService.getAllBudgets();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(budgetId, result.get(0).getId());
        verify(budgetRepository, times(1)).findAll();
        verify(budgetMapper, times(1)).toResponseDTO(budget);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há orçamentos")
    void shouldReturnEmptyListWhenNoBudgets() {
        // Arrange
        when(budgetRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<BudgetResponseDTO> result = budgetService.getAllBudgets();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(budgetRepository, times(1)).findAll();
        verify(budgetMapper, never()).toResponseDTO(any());
    }
}

