package com.indux.core.infra.exception.user;

public class NotFoundEmployee extends RuntimeException {
    public NotFoundEmployee(String message) {
        super(message);
    }
}
