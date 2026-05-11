package com.indux.core.presentation;

import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/employee")
public class MobileEmployeeController {

    private final GetEmployeeUseCase getEmployeeUseCase;

    public MobileEmployeeController(GetEmployeeUseCase getEmployeeUseCase) {
        this.getEmployeeUseCase = getEmployeeUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<CompleteEmployeeDTO> getAuthenticatedEmployee(JwtAuthenticationToken jwt) {
        String cpf = jwt.getToken().getClaimAsString("cpf");
        CompleteEmployeeDTO employee = getEmployeeUseCase.getMobileEmployeeByCpf(cpf);
        return ResponseEntity.ok(employee);
    }
}
