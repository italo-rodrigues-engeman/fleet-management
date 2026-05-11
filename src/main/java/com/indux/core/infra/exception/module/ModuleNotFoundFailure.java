package com.indux.core.infra.exception.module;

public class ModuleNotFoundFailure extends RuntimeException {
    public ModuleNotFoundFailure(String message) {
        super(message);
    }
}
