package com.indux.core.infra.exception.user;

public class UserBadCredentials extends RuntimeException {
    public UserBadCredentials(String message) {
        super(message);
    }
}
