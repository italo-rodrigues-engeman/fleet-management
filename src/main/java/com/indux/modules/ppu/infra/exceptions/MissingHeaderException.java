package com.indux.modules.ppu.infra.exceptions;

public class MissingHeaderException extends RuntimeException {
    public MissingHeaderException(String message) {
        super(message);
    }
}