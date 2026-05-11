package com.indux.core.infra.exception.module;

public class ModuleFailure extends RuntimeException {
    public ModuleFailure(String message) {
        super(message);
    }
}
