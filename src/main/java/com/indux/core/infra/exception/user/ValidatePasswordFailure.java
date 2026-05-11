package com.indux.core.infra.exception.user;

public class ValidatePasswordFailure extends RuntimeException {
    public ValidatePasswordFailure(String message) {
        super(message);
    }
}
