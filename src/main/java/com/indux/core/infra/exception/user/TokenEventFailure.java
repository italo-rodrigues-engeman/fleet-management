package com.indux.core.infra.exception.user;

public class TokenEventFailure extends RuntimeException {
    public TokenEventFailure(String message) {
        super(message);
    }
}
