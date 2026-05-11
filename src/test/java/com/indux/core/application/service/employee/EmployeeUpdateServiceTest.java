package com.indux.core.application.service.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeUpdateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeUpdateService employeeUpdateService;

    private final String UPDATE_URL = "http://192.168.0.10:3031/get-employee-update";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return ResponseEntity when API call is successful")
    void shouldReturnResponseEntityWhenApiCallIsSuccessful() {
        Object mockBody = new Object();
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);

        when(restTemplate.exchange(eq(UPDATE_URL), eq(HttpMethod.GET), isNull(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = employeeUpdateService.getEmployeeUpdate();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }

    @Test
    @DisplayName("Should throw RuntimeException when API call fails")
    void shouldThrowRuntimeExceptionWhenApiCallFails() {
        when(restTemplate.exchange(eq(UPDATE_URL), eq(HttpMethod.GET), isNull(), eq(Object.class)))
                .thenThrow(new RestClientException("Connection Refused"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeUpdateService.getEmployeeUpdate();
        });

        assertTrue(exception.getMessage().contains("Erro ao chamar API externa: Connection Refused"));
    }
}
