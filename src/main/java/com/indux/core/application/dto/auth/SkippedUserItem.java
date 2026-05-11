package com.indux.core.application.dto.auth;

public record SkippedUserItem(
        String matricula,
        SkipReason reason
) {
    public enum SkipReason {
        NOT_FOUND,
        ALREADY_EXISTS,
        INVALID_DATA
    }
}
