package com.indux.core.infra.exception.module;

public class ForbiddenModuleAccessException extends RuntimeException {
    public ForbiddenModuleAccessException(String message) {
        super(message);
    }
}
