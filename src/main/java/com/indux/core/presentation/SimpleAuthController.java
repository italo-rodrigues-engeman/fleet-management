package com.indux.core.presentation;

import com.indux.core.application.dto.auth.SimpleLoginRequestDTO;
import com.indux.core.application.dto.auth.SimpleLoginResponseDTO;
import com.indux.core.application.service.auth.SimpleLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/simple")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimpleAuthController {
    
    private final SimpleLoginService simpleLoginService;
    
    @PostMapping("/login")
    public ResponseEntity<SimpleLoginResponseDTO> login(@Valid @RequestBody SimpleLoginRequestDTO loginRequest) {
        log.info("Recebida requisição de login simples para CPF: {}", maskCpf(loginRequest.getCpf()));
        
        SimpleLoginResponseDTO response = simpleLoginService.authenticate(loginRequest);
        
        if (response.isSuccess()) {
            log.info("Login simples realizado com sucesso para funcionário: {} - {}", 
                    response.getMatricula(), response.getNome());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Falha no login simples para CPF: {} - Motivo: {}", 
                    maskCpf(loginRequest.getCpf()), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Simple Auth Service is running");
    }
    
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        return "***" + cpf.substring(cpf.length() - 4);
    }
}
