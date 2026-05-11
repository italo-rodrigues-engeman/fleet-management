package com.indux.core.application.service.employee;

import com.indux.core.application.dto.generic.EmployeeFiltersDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeFiltersUseCaseTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query filialQuery;

    @Mock
    private Query situacaoQuery;

    @Mock
    private Query estadoQuery;

    @Mock
    private Query grauInstrucaoQuery;

    @Mock
    private Query dependentesQuery;

    @InjectMocks
    private GetEmployeeFiltersUseCase getEmployeeFiltersUseCase;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return filters correctly mapped")
    void shouldReturnFiltersCorrectlyMapped() {
        ReflectionTestUtils.setField(getEmployeeFiltersUseCase, "em", entityManager);
        
        when(entityManager.createNativeQuery(contains("tb_filiais"))).thenReturn(filialQuery);
        when(entityManager.createNativeQuery(contains("situacao FROM"))).thenReturn(situacaoQuery);
        when(entityManager.createNativeQuery(contains("estado FROM"))).thenReturn(estadoQuery);
        when(entityManager.createNativeQuery(contains("grau_instrucao FROM"))).thenReturn(grauInstrucaoQuery);
        when(entityManager.createNativeQuery(contains("qtd_dependentes FROM"))).thenReturn(dependentesQuery);

        Object[] filialRow = new Object[] { 1L, "Matriz" };
        when(filialQuery.getResultList()).thenReturn(Collections.singletonList(filialRow));
        when(situacaoQuery.getResultList()).thenReturn(List.of("Trabalhando"));
        when(estadoQuery.getResultList()).thenReturn(List.of("SP"));
        when(grauInstrucaoQuery.getResultList()).thenReturn(List.of("Superior Completo"));
        when(dependentesQuery.getResultList()).thenReturn(List.of(2));

        EmployeeFiltersDTO result = getEmployeeFiltersUseCase.getFilters();

        assertNotNull(result);

        assertEquals(1, result.getFiliais().size());
        assertEquals(1L, result.getFiliais().get(0).getCodigo_filial());
        assertEquals("Matriz", result.getFiliais().get(0).getNome_filial());

        assertEquals(1, result.getSituacoes().size());
        assertEquals("Trabalhando", result.getSituacoes().get(0));

        assertEquals(1, result.getEstados().size());
        assertEquals("SP", result.getEstados().get(0));

        assertEquals(1, result.getGraus_instrucao().size());
        assertEquals("Superior Completo", result.getGraus_instrucao().get(0));

        assertEquals(1, result.getQtd_dependentes().size());
        assertEquals(2, result.getQtd_dependentes().get(0));
    }
}
