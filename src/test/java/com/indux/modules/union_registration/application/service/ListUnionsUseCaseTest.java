package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.ListUnionsFilterDTO;
import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ListUnionsUseCaseTest {
    
    private UnionRepository unionRepository;
    private UnionMapper unionMapper;
    private ListUnionsUseCase listUnionsUseCase;
    
    @BeforeEach
    void setUp() {
        unionRepository = mock(UnionRepository.class);
        unionMapper = mock(UnionMapper.class);
        listUnionsUseCase = new ListUnionsUseCase(unionRepository, unionMapper);
    }
    
    @Test
    @DisplayName("Deve listar todos os sindicatos sem filtros")
    void shouldListAllUnionsWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Union> unions = createUnionsList();
        Page<Union> unionPage = new PageImpl<>(unions, pageable, unions.size());
        
        List<UnionResponseDTO> unionDTOs = createUnionResponseList();
        
        when(unionRepository.findAll(pageable)).thenReturn(unionPage);
        when(unionMapper.toResponseDTO(any(Union.class)))
                .thenReturn(unionDTOs.get(0), unionDTOs.get(1));
        
        Page<UnionResponseDTO> result = listUnionsUseCase.listAll(pageable);
        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getNumber());
        
        verify(unionRepository).findAll(pageable);
        verify(unionMapper, times(2)).toResponseDTO(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve listar sindicatos com filtros")
    void shouldListUnionsWithFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        ListUnionsFilterDTO filters = new ListUnionsFilterDTO();
        filters.setNomeCompletoSindicato("Sindicato Teste");
        filters.setStatusRegistro("ATIVO");
        
        List<Union> unions = createUnionsList();
        Page<Union> unionPage = new PageImpl<>(unions, pageable, unions.size());
        
        List<UnionResponseDTO> unionDTOs = createUnionResponseList();
        
        when(unionRepository.findAllWithFilters(eq(filters), eq(pageable))).thenReturn(unionPage);
        
        when(unionMapper.toResponseDTO(any(Union.class)))
                .thenReturn(unionDTOs.get(0), unionDTOs.get(1));
        
        Page<UnionResponseDTO> result = listUnionsUseCase.execute(filters, pageable);
        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        
        verify(unionRepository).findAllWithFilters(eq(filters), eq(pageable));
        verify(unionMapper, times(2)).toResponseDTO(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve retornar página vazia quando não encontrar sindicatos")
    void shouldReturnEmptyPageWhenNoUnionsFound() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Union> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(unionRepository.findAll(pageable)).thenReturn(emptyPage);
        
        Page<UnionResponseDTO> result = listUnionsUseCase.listAll(pageable);
        
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getNumber());
        
        verify(unionRepository).findAll(pageable);
        verify(unionMapper, never()).toResponseDTO(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve aplicar paginação corretamente")
    void shouldApplyPaginationCorrectly() {
        Pageable pageable = PageRequest.of(1, 10); // Segunda página, 10 itens por página
        List<Union> unions = createUnionsList();
        Page<Union> unionPage = new PageImpl<>(unions, pageable, 25); // Total de 25 itens
        
        List<UnionResponseDTO> unionDTOs = createUnionResponseList();
        
        when(unionRepository.findAll(pageable)).thenReturn(unionPage);
        when(unionMapper.toResponseDTO(any(Union.class)))
                .thenReturn(unionDTOs.get(0), unionDTOs.get(1));
        
        Page<UnionResponseDTO> result = listUnionsUseCase.listAll(pageable);
        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(25, result.getTotalElements()); // Total de elementos
        assertEquals(1, result.getNumber()); // Página atual
        assertEquals(10, result.getSize()); // Tamanho da página
        
        verify(unionRepository).findAll(pageable);
    }
    
    private List<Union> createUnionsList() {
        Union union1 = new Union();
        union1.setId("1");
        union1.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste 1");
        union1.setCnpj("12.345.678/0001-90");
        union1.setStatusRegistro("ATIVO");
        
        Union union2 = new Union();
        union2.setId("2");
        union2.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste 2");
        union2.setCnpj("98.765.432/0001-10");
        union2.setStatusRegistro("ATIVO");
        
        return Arrays.asList(union1, union2);
    }
    
    private List<UnionResponseDTO> createUnionResponseList() {
        UnionResponseDTO dto1 = new UnionResponseDTO();
        dto1.setId("1");
        dto1.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste 1");
        dto1.setCnpj("12.345.678/0001-90");
        dto1.setStatusRegistro("ATIVO");
        
        UnionResponseDTO dto2 = new UnionResponseDTO();
        dto2.setId("2");
        dto2.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste 2");
        dto2.setCnpj("98.765.432/0001-10");
        dto2.setStatusRegistro("ATIVO");
        
        return Arrays.asList(dto1, dto2);
    }
}

