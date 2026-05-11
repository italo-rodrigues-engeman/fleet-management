package com.indux.modules.union_registration.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteLaborContractUseCaseTest {
    
    private LaborContractRepository laborContractRepository;
    private LaborContractAddendumRepository addendumRepository;
    private DeleteLaborContractUseCase deleteLaborContractUseCase;
    
    @BeforeEach
    void setUp() {
        laborContractRepository = mock(LaborContractRepository.class);
        addendumRepository = mock(LaborContractAddendumRepository.class);
        deleteLaborContractUseCase = new DeleteLaborContractUseCase(laborContractRepository, addendumRepository);
    }
    
    @Test
    @DisplayName("Deve excluir contrato trabalhista permanentemente com sucesso")
    void shouldDeleteLaborContractPermanently() {
        String contractId = "contrato123";
        LaborContract laborContract = createActiveLaborContract();
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findByContratoTrabalhistaId(contractId)).thenReturn(Collections.emptyList());
        
        GenericMessage result = deleteLaborContractUseCase.execute(contractId);
        
        assertNotNull(result);
        assertEquals("Contrato trabalhista e seus aditivos excluídos com sucesso", result.message());
        assertEquals(200, result.status());
        
        verify(laborContractRepository).findById(contractId);
        verify(addendumRepository).findByContratoTrabalhistaId(contractId);
        verify(laborContractRepository).delete(laborContract);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato não existe")
    void shouldThrowExceptionWhenContractNotFound() {
        String contractId = "contrato_inexistente";
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteLaborContractUseCase.execute(contractId));
        
        assertTrue(exception.getMessage().contains("Contrato trabalhista não encontrado com ID: " + contractId));
        verify(laborContractRepository).findById(contractId);
        verify(laborContractRepository, never()).delete(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato já está inativo")
    void shouldThrowExceptionWhenContractAlreadyInactive() {
        String contractId = "contrato123";
        LaborContract laborContract = createInactiveLaborContract();
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.of(laborContract));
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteLaborContractUseCase.execute(contractId));
        
        assertTrue(exception.getMessage().contains("inativo"));
        verify(laborContractRepository).findById(contractId);
        verify(laborContractRepository, never()).delete(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve desativar contrato trabalhista com sucesso")
    void shouldSoftDeleteLaborContractSuccessfully() {
        String contractId = "contrato123";
        LaborContract laborContract = createActiveLaborContract();
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findByContratoTrabalhistaId(contractId)).thenReturn(Collections.emptyList());
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(laborContract);
        
        GenericMessage result = deleteLaborContractUseCase.softDelete(contractId);
        
        assertNotNull(result);
        assertEquals("Contrato trabalhista e seus aditivos desativados com sucesso", result.message());
        assertEquals(200, result.status());
        assertEquals("INATIVO", laborContract.getStatusRegistro());
        
        verify(laborContractRepository).findById(contractId);
        verify(addendumRepository).findByContratoTrabalhistaId(contractId);
        verify(laborContractRepository).save(laborContract);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato não existe no soft delete")
    void shouldThrowExceptionWhenContractNotFoundInSoftDelete() {
        String contractId = "contrato_inexistente";
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteLaborContractUseCase.softDelete(contractId));
        
        assertTrue(exception.getMessage().contains("Contrato trabalhista não encontrado com ID: " + contractId));
        verify(laborContractRepository).findById(contractId);
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato já está inativo no soft delete")
    void shouldThrowExceptionWhenContractAlreadyInactiveInSoftDelete() {
        String contractId = "contrato123";
        LaborContract laborContract = createInactiveLaborContract();
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.of(laborContract));
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteLaborContractUseCase.softDelete(contractId));
        
        assertTrue(exception.getMessage().contains("Contrato já está inativo"));
        verify(laborContractRepository).findById(contractId);
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve verificar status antes de excluir")
    void shouldCheckStatusBeforeDeleting() {
        String contractId = "contrato123";
        LaborContract laborContract = createActiveLaborContract();
        laborContract.setStatusRegistro("ATIVO");
        
        when(laborContractRepository.findById(contractId)).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findByContratoTrabalhistaId(contractId)).thenReturn(Collections.emptyList());
        
        GenericMessage result = deleteLaborContractUseCase.execute(contractId);
        
        assertNotNull(result);
        assertEquals("Contrato trabalhista e seus aditivos excluídos com sucesso", result.message());
        verify(laborContractRepository).delete(laborContract);
    }
    
    private LaborContract createActiveLaborContract() {
        LaborContract laborContract = new LaborContract();
        laborContract.setId("contrato123");
        laborContract.setTipoInstrumento(TipoInstrumento.ACT);
        laborContract.setNumeroIdentificacaoInterno("ACT-2024-001");
        laborContract.setNomeInstrumento("Acordo Coletivo de Trabalho 2024");
        laborContract.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        laborContract.setDataInicioVigencia(LocalDate.now());
        laborContract.setDataFimVigencia(LocalDate.now().plusYears(1));
        laborContract.setStatusRegistro("ATIVO");
        laborContract.setDataCriacao(LocalDateTime.now());
        laborContract.setUsuarioCriacao("usuario.teste");
        return laborContract;
    }
    
    private LaborContract createInactiveLaborContract() {
        LaborContract laborContract = createActiveLaborContract();
        laborContract.setStatusRegistro("INATIVO");
        return laborContract;
    }
}
