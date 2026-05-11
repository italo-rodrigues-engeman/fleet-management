package com.indux.modules.ppu.infra.exceptions;

public class InvalidExcelFileException extends RuntimeException {
    public InvalidExcelFileException(String message) {
        super(message);
    }
}