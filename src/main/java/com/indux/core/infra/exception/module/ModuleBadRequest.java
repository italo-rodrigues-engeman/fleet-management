package com.indux.core.infra.exception.module;

public class ModuleBadRequest extends RuntimeException {
    public ModuleBadRequest(String message) {
        super(message);
    }
}
