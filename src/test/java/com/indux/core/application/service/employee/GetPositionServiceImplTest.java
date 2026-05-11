package com.indux.core.application.service.employee;

import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.repository.employee.EmployeePositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPositionServiceImplTest {

    @Mock
    private EmployeePositionRepository employeePositionRepository;

    @InjectMocks
    private GetPositionServiceImpl getPositionService;

    private EmployeePosition positionA;
    private EmployeePosition positionB;

    @BeforeEach
    void setUp() {
        positionA = EmployeePosition.builder()
                .codeCbo("123")
                .idHCM("HCM1")
                .build();

        positionB = EmployeePosition.builder()
                .codeCbo("123")
                .idHCM("HCM2")
                .build();
    }

    @Test
    @DisplayName("Should find HCM codes by CBO code")
    void shouldFindHcmCodesByCboCode() {
        when(employeePositionRepository.findByCodeCbo(anyString())).thenReturn(List.of(positionA, positionB));

        List<String> result = getPositionService.findHcmCodesByCboCode("123");

        assertEquals(2, result.size());
        assertTrue(result.contains("HCM1"));
        assertTrue(result.contains("HCM2"));
    }

    @Test
    @DisplayName("Should find HCM codes by CBO codes")
    void shouldFindHCMCodesByCboCodes() {
        when(employeePositionRepository.findByCodeCboIn(anyList())).thenReturn(List.of(positionA, positionB));

        List<String> result = getPositionService.findHCMCodesByCboCodes(List.of("123", "456"));

        assertEquals(2, result.size());
        assertTrue(result.contains("HCM1"));
        assertTrue(result.contains("HCM2"));
    }

    @Test
    @DisplayName("Should return empty list when CBO code not found")
    void shouldReturnEmptyListWhenCboCodeNotFound_findByCboCode() {
        when(employeePositionRepository.findByCodeCbo(anyString())).thenReturn(null);

        List<EmployeePosition> result = getPositionService.findByCboCode("999");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when CBO codes not found")
    void shouldReturnEmptyListWhenCboCodesNotFound_findByCboCodes() {
        when(employeePositionRepository.findByCodeCboIn(anyList())).thenReturn(null);

        List<EmployeePosition> result = getPositionService.findByCboCodes(List.of("999"));

        assertTrue(result.isEmpty());
    }
}
