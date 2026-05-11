package com.indux.core.infra.exception;

public class NotFoundBankDetails extends RuntimeException {
    public NotFoundBankDetails(String message) {
        super(message);
    }
}
