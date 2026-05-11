package com.indux.modules.union_registration.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteUnionUseCaseTest {
    
    private UnionRepository unionRepository;
    private LaborContractRepository laborContractRepository;
    private LaborContractAddendumRepository addendumRepository;
    private DeleteUnionUseCase deleteUnionUseCase;
    
    @BeforeEach
    void setUp() {
        unionRepository = mock(UnionRepository.class);
        laborContractRepository = mock(LaborContractRepository.class);
        addendumRepository = mock(LaborContractAddendumRepository.class);
        deleteUnionUseCase = new DeleteUnionUseCase(unionRepository, laborContractRepository, addendumRepository);
        
        // Mock padrão para retornar lista vazia de contratos
        when(laborContractRepository.findBySindicatoTrabalhadoresId(anyString())).thenReturn(Collections.emptyList());
    }
    
    @Test
    @DisplayName("Deve excluir sindicato com sucesso")
    void shouldDeleteUnionSuccessfully() {
        String unionId = "64f8b2c1234567890abcdef1";
        Union union = createActiveUnion();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        
        GenericMessage result = deleteUnionUseCase.execute(unionId);
        
        assertNotNull(result);
        assertEquals("Sindicato, seus contratos e aditivos excluídos com sucesso", result.message());
        assertEquals(200, result.status());
        
        verify(unionRepository).findById(unionId);
        verify(unionRepository).delete(union);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando sindicato não existe")
    void shouldThrowExceptionWhenUnionNotFound() {
        String unionId = "64f8b2c1234567890abcdef1";
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteUnionUseCase.execute(unionId));
        
        assertEquals("Sindicato não encontrado com ID: " + unionId, exception.getMessage());
        verify(unionRepository).findById(unionId);
        verify(unionRepository, never()).delete(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve desativar sindicato com sucesso (soft delete)")
    void shouldSoftDeleteUnionSuccessfully() {
        String unionId = "64f8b2c1234567890abcdef1";
        Union union = createActiveUnion();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        when(unionRepository.save(any(Union.class))).thenReturn(union);
        
        GenericMessage result = deleteUnionUseCase.softDelete(unionId);
        
        assertNotNull(result);
        assertEquals("Sindicato, seus contratos e aditivos desativados com sucesso", result.message());
        assertEquals(200, result.status());
        assertEquals("INATIVO", union.getStatusRegistro());
        
        verify(unionRepository).findById(unionId);
        verify(unionRepository).save(union);
        verify(unionRepository, never()).delete(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar desativar sindicato já inativo")
    void shouldThrowExceptionWhenTryingToDeactivateInactiveUnion() {
        String unionId = "64f8b2c1234567890abcdef1";
        Union union = createInactiveUnion();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteUnionUseCase.softDelete(unionId));
        
        assertEquals("Sindicato já está inativo", exception.getMessage());
        verify(unionRepository).findById(unionId);
        verify(unionRepository, never()).save(any(Union.class));
        verify(unionRepository, never()).delete(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir sindicato já inativo")
    void shouldThrowExceptionWhenTryingToDeleteInactiveUnion() {
        String unionId = "64f8b2c1234567890abcdef1";
        Union union = createInactiveUnion();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(union));
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteUnionUseCase.execute(unionId));
        
        assertEquals("Sindicato já está inativo", exception.getMessage());
        verify(unionRepository).findById(unionId);
        verify(unionRepository, never()).delete(any(Union.class));
    }
    
    private Union createActiveUnion() {
        Union union = new Union();
        union.setId("64f8b2c1234567890abcdef1");
        union.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste");
        union.setCnpj("12.345.678/0001-90");
        union.setStatusRegistro("ATIVO");
        return union;
    }
    
    private Union createInactiveUnion() {
        Union union = new Union();
        union.setId("64f8b2c1234567890abcdef1");
        union.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste");
        union.setCnpj("12.345.678/0001-90");
        union.setStatusRegistro("INATIVO");
        return union;
    }
}
