package com.indux.core.presentation;

import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MobileEmployeeControllerTest {

    @Mock
    private GetEmployeeUseCase getEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        MobileEmployeeController controller = new MobileEmployeeController(getEmployeeUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnAuthenticatedEmployeeForMobileUser() throws Exception {
        UUID employeeId = UUID.randomUUID();
        CompleteEmployeeDTO employeeDTO = CompleteEmployeeDTO.builder()
                .id(employeeId)
                .matricula("MAT-ATIVA")
                .nome("Colaborador Mobile")
                .build();

        when(getEmployeeUseCase.getMobileEmployeeByCpf("12345678900")).thenReturn(employeeDTO);

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", employeeId.toString(),
                        "cpf", "12345678900",
                        "roles", List.of("MOBILE_ACCESS")));

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

        mockMvc.perform(get("/api/mobile/employee/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.matricula").value("MAT-ATIVA"))
                .andExpect(jsonPath("$.nome").value("Colaborador Mobile"));

        verify(getEmployeeUseCase).getMobileEmployeeByCpf("12345678900");
    }
}
