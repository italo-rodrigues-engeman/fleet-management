package com.indux.core.presentation.handler;

import com.indux.core.domain.exception.DuplicateContractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ContractDuplicateErrorHandler {

    @ExceptionHandler(DuplicateContractException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateContractException(DuplicateContractException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Erro de Contrato Duplicado");
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
} 