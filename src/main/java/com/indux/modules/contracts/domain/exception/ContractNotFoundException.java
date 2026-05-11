package com.indux.modules.contracts.domain.exception;

public class ContractNotFoundException extends RuntimeException {
    
    public ContractNotFoundException(String message) {
        super(message);
    }
    
    public ContractNotFoundException(Long id) {
        super("Contrato não encontrado com ID: " + id);
    }
} 