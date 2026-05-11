package com.indux.modules.ocf.application.service;

import com.indux.modules.organization_chart.application.dtos.AllSubordinatesResponseDTO;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.domain.repositories.jpa.SimpleContractRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.SimpleProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiretoriaFilterServiceTest {

    @Mock
    private SubordinateService subordinateService;

    @Mock
    private SimpleContractRepository contractRepository;

    @Mock
    private SimpleProjectRepository projectRepository;

    @InjectMocks
    private DiretoriaFilterService diretoriaFilterService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getHcmIdsByDiretorias_ComListaVazia_DeveRetornarSetVazio() {
        List<Long> diretoriaIds = List.of();

        Set<String> result = diretoriaFilterService.getHcmIdsByDiretorias(diretoriaIds);

        assertTrue(result.isEmpty());
        verifyNoInteractions(subordinateService);
    }

    @Test
    void getHcmIdsByDiretorias_ComListaNula_DeveRetornarSetVazio() {
        List<Long> diretoriaIds = null;

        Set<String> result = diretoriaFilterService.getHcmIdsByDiretorias(diretoriaIds);

        assertTrue(result.isEmpty());
        verifyNoInteractions(subordinateService);
    }

    @Test
    void isCentroCustosInDiretorias_ComCentroCustosNulo_DeveRetornarFalse() {
        String centroCustosId = null;
        List<Long> diretoriaIds = List.of(1L, 2L);

        boolean result = diretoriaFilterService.isCentroCustosInDiretorias(centroCustosId, diretoriaIds);

        assertFalse(result);
        verifyNoInteractions(subordinateService);
    }

    @Test
    void isCentroCustosInDiretorias_ComCentroCustosVazio_DeveRetornarFalse() {
        String centroCustosId = "";
        List<Long> diretoriaIds = List.of(1L, 2L);

        boolean result = diretoriaFilterService.isCentroCustosInDiretorias(centroCustosId, diretoriaIds);

        assertFalse(result);
        verifyNoInteractions(subordinateService);
    }

    @Test
    void isCentroCustosInDiretorias_ComListaDiretoriasNula_DeveRetornarFalse() {
        String centroCustosId = "12345";
        List<Long> diretoriaIds = null;

        boolean result = diretoriaFilterService.isCentroCustosInDiretorias(centroCustosId, diretoriaIds);

        assertFalse(result);
        verifyNoInteractions(subordinateService);
    }

    @Test
    void isCentroCustosInDiretorias_ComListaDiretoriasVazia_DeveRetornarFalse() {
        String centroCustosId = "12345";
        List<Long> diretoriaIds = List.of();

        boolean result = diretoriaFilterService.isCentroCustosInDiretorias(centroCustosId, diretoriaIds);

        assertFalse(result);
        verifyNoInteractions(subordinateService);
    }

    @Test
    void getHcmIdsByDiretorias_ComDiretoriaValida_DeveChamarMetodosCorretos() {
        List<Long> diretoriaIds = List.of(1L);
        AllSubordinatesResponseDTO mockResponse = mock(AllSubordinatesResponseDTO.class);
        
        when(subordinateService.getAllSubordinatesRecursivelyByDirectorId(1L)).thenReturn(mockResponse);
        when(contractRepository.findBySubordinateIdIn(any())).thenReturn(List.of());
        when(projectRepository.findBySubordinateIdInAndContractIdIsNull(any())).thenReturn(List.of());

        Set<String> result = diretoriaFilterService.getHcmIdsByDiretorias(diretoriaIds);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subordinateService).getAllSubordinatesRecursivelyByDirectorId(1L);
        verify(contractRepository).findBySubordinateIdIn(any());
        verify(projectRepository).findBySubordinateIdInAndContractIdIsNull(any());
    }
}