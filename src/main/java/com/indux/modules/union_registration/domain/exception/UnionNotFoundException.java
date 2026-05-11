package com.indux.modules.union_registration.domain.exception;

public class UnionNotFoundException extends RuntimeException {
    public UnionNotFoundException(String message) {
        super(message);
    }
    
    public UnionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
