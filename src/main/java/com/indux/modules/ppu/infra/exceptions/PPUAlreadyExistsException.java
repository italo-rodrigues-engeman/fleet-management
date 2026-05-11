package com.indux.modules.ppu.infra.exceptions;

public class PPUAlreadyExistsException extends RuntimeException {
    public PPUAlreadyExistsException(String message) {
        super(message);
    }
}
