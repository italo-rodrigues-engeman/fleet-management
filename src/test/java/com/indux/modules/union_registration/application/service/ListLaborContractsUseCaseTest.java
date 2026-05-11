package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ListLaborContractsUseCaseTest {
    
    private LaborContractRepository laborContractRepository;
    private LaborContractAddendumRepository addendumRepository;
    private UnionRepository unionRepository;
    private LaborContractMapper laborContractMapper;
    private ListLaborContractsUseCase listLaborContractsUseCase;
    
    @BeforeEach
    void setUp() {
        laborContractRepository = mock(LaborContractRepository.class);
        addendumRepository = mock(LaborContractAddendumRepository.class);
        unionRepository = mock(UnionRepository.class);
        laborContractMapper = mock(LaborContractMapper.class);
        listLaborContractsUseCase = new ListLaborContractsUseCase(
            laborContractRepository, addendumRepository, unionRepository, laborContractMapper);
        
        // Mock padrão para retornar null para última sequência de aditivo
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc(anyString())).thenReturn(null);
    }
    
    @Test
    @DisplayName("Deve listar contratos com paginação com sucesso")
    void shouldListLaborContractsWithPaginationSuccessfully() {
        String unionId = "68cd2d492329c37e18b00944";
        Pageable pageable = PageRequest.of(0, 20);
        
        Union union = createUnionEntity();
        List<LaborContract> contracts = Arrays.asList(
            createLaborContractEntity("contrato1", TipoInstrumento.ACT),
            createLaborContractEntity("contrato2", TipoInstrumento.CCT)
        );
        Page<LaborContract> contractsPage = new PageImpl<>(contracts, pageable, 2);
        
        LaborContractResponseDTO response1 = createLaborContractResponse("contrato1", TipoInstrumento.ACT);
        LaborContractResponseDTO response2 = createLaborContractResponse("contrato2", TipoInstrumento.CCT);
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        when(laborContractRepository.findBySindicatoTrabalhadoresId(unionId, pageable))
            .thenReturn(contractsPage);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(0), union))
            .thenReturn(response1);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(1), union))
            .thenReturn(response2);
        
        Page<LaborContractResponseDTO> result = listLaborContractsUseCase.execute(unionId, pageable);
        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(TipoInstrumento.ACT, result.getContent().get(0).getTipoInstrumento());
        assertEquals(TipoInstrumento.CCT, result.getContent().get(1).getTipoInstrumento());
        assertEquals(0, result.getNumber());
        assertEquals(20, result.getSize());
        assertEquals(2, result.getTotalElements());
        
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository).findBySindicatoTrabalhadoresId(unionId, pageable);
        verify(laborContractMapper, times(2)).toResponseDTOWithUnionInfo(any(LaborContract.class), eq(union));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando sindicato não existe")
    void shouldThrowExceptionWhenUnionNotFound() {
        String unionId = "sindicato_inexistente";
        Pageable pageable = PageRequest.of(0, 20);
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> listLaborContractsUseCase.execute(unionId, pageable));
        
        assertTrue(exception.getMessage().contains("Sindicato não encontrado com ID: " + unionId));
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository, never()).findBySindicatoTrabalhadoresId(anyString(), any(Pageable.class));
    }
    
    @Test
    @DisplayName("Deve listar todos os contratos sem paginação")
    void shouldListAllLaborContractsWithoutPagination() {
        String unionId = "68cd2d492329c37e18b00944";
        
        Union union = createUnionEntity();
        List<LaborContract> contracts = Arrays.asList(
            createLaborContractEntity("contrato1", TipoInstrumento.ACT),
            createLaborContractEntity("contrato2", TipoInstrumento.CCT),
            createLaborContractEntity("contrato3", TipoInstrumento.ACT)
        );
        
        LaborContractResponseDTO response1 = createLaborContractResponse("contrato1", TipoInstrumento.ACT);
        LaborContractResponseDTO response2 = createLaborContractResponse("contrato2", TipoInstrumento.CCT);
        LaborContractResponseDTO response3 = createLaborContractResponse("contrato3", TipoInstrumento.ACT);
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        when(laborContractRepository.findBySindicatoTrabalhadoresId(unionId)).thenReturn(contracts);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(0), union))
            .thenReturn(response1);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(1), union))
            .thenReturn(response2);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(2), union))
            .thenReturn(response3);
        
        List<LaborContractResponseDTO> result = listLaborContractsUseCase.executeAll(unionId);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(TipoInstrumento.ACT, result.get(0).getTipoInstrumento());
        assertEquals(TipoInstrumento.CCT, result.get(1).getTipoInstrumento());
        assertEquals(TipoInstrumento.ACT, result.get(2).getTipoInstrumento());
        
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository).findBySindicatoTrabalhadoresId(unionId);
        verify(laborContractMapper, times(3)).toResponseDTOWithUnionInfo(any(LaborContract.class), eq(union));
    }
    
    @Test
    @DisplayName("Deve filtrar contratos por tipo com paginação")
    void shouldFilterContractsByTypeWithPagination() {
        String unionId = "68cd2d492329c37e18b00944";
        TipoInstrumento tipoInstrumento = TipoInstrumento.ACT;
        Pageable pageable = PageRequest.of(0, 20);
        
        Union union = createUnionEntity();
        List<LaborContract> contracts = Arrays.asList(
            createLaborContractEntity("contrato1", TipoInstrumento.ACT),
            createLaborContractEntity("contrato2", TipoInstrumento.ACT)
        );
        Page<LaborContract> contractsPage = new PageImpl<>(contracts, pageable, 2);
        
        LaborContractResponseDTO response1 = createLaborContractResponse("contrato1", TipoInstrumento.ACT);
        LaborContractResponseDTO response2 = createLaborContractResponse("contrato2", TipoInstrumento.ACT);
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        when(laborContractRepository.findBySindicatoTrabalhadoresIdAndTipoInstrumentoAndStatusRegistro(
            unionId, tipoInstrumento, "ATIVO", pageable)).thenReturn(contractsPage);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(0), union))
            .thenReturn(response1);
        when(laborContractMapper.toResponseDTOWithUnionInfo(contracts.get(1), union))
            .thenReturn(response2);
        
        Page<LaborContractResponseDTO> result = listLaborContractsUseCase.executeByType(unionId, tipoInstrumento, pageable);
        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(TipoInstrumento.ACT, result.getContent().get(0).getTipoInstrumento());
        assertEquals(TipoInstrumento.ACT, result.getContent().get(1).getTipoInstrumento());
        
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository).findBySindicatoTrabalhadoresIdAndTipoInstrumentoAndStatusRegistro(
            unionId, tipoInstrumento, "ATIVO", pageable);
        verify(laborContractMapper, times(2)).toResponseDTOWithUnionInfo(any(LaborContract.class), eq(union));
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há contratos")
    void shouldReturnEmptyListWhenNoContracts() {
        String unionId = "68cd2d492329c37e18b00944";
        Pageable pageable = PageRequest.of(0, 20);
        
        Union union = createUnionEntity();
        Page<LaborContract> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        when(laborContractRepository.findBySindicatoTrabalhadoresId(unionId, pageable))
            .thenReturn(emptyPage);
        
        Page<LaborContractResponseDTO> result = listLaborContractsUseCase.execute(unionId, pageable);
        
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository).findBySindicatoTrabalhadoresId(unionId, pageable);
        verify(laborContractMapper, never()).toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando sindicato não existe no executeAll")
    void shouldThrowExceptionWhenUnionNotFoundInExecuteAll() {
        String unionId = "sindicato_inexistente";
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> listLaborContractsUseCase.executeAll(unionId));
        
        assertTrue(exception.getMessage().contains("Sindicato não encontrado com ID: " + unionId));
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository, never()).findBySindicatoTrabalhadoresId(anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando sindicato não existe no executeByType")
    void shouldThrowExceptionWhenUnionNotFoundInExecuteByType() {
        String unionId = "sindicato_inexistente";
        TipoInstrumento tipoInstrumento = TipoInstrumento.ACT;
        Pageable pageable = PageRequest.of(0, 20);
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> listLaborContractsUseCase.executeByType(unionId, tipoInstrumento, pageable));
        
        assertTrue(exception.getMessage().contains("Sindicato não encontrado com ID: " + unionId));
        verify(unionRepository).findById(unionId);
        verify(laborContractRepository, never()).findBySindicatoTrabalhadoresIdAndTipoInstrumentoAndStatusRegistro(
            anyString(), any(TipoInstrumento.class), anyString(), any(Pageable.class));
    }
    
    private Union createUnionEntity() {
        Union union = new Union();
        union.setId("68cd2d492329c37e18b00944");
        union.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Tecnologia");
        union.setCnpj("12.345.678/0001-90");
        union.setStatusRegistro("ATIVO");
        return union;
    }
    
    private LaborContract createLaborContractEntity(String id, TipoInstrumento tipoInstrumento) {
        LaborContract laborContract = new LaborContract();
        laborContract.setId(id);
        laborContract.setTipoInstrumento(tipoInstrumento);
        laborContract.setNumeroIdentificacaoInterno(id + "-2024-001");
        laborContract.setNomeInstrumento("Instrumento " + tipoInstrumento + " 2024");
        laborContract.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        laborContract.setDataInicioVigencia(LocalDate.now());
        laborContract.setDataFimVigencia(LocalDate.now().plusYears(1));
        laborContract.setStatusRegistro("ATIVO");
        laborContract.setDataCriacao(LocalDateTime.now());
        laborContract.setUsuarioCriacao("usuario.teste");
        return laborContract;
    }
    
    private LaborContractResponseDTO createLaborContractResponse(String id, TipoInstrumento tipoInstrumento) {
        LaborContractResponseDTO response = new LaborContractResponseDTO();
        response.setId(id);
        response.setTipoInstrumento(tipoInstrumento);
        response.setNumeroIdentificacaoInterno(id + "-2024-001");
        response.setNomeInstrumento("Instrumento " + tipoInstrumento + " 2024");
        response.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        response.setNomeSindicato("Sindicato dos Trabalhadores em Tecnologia");
        response.setSiglaSindicato("STT");
        response.setStatusRegistro("ATIVO");
        response.setDataCriacao(LocalDateTime.now());
        return response;
    }
}
