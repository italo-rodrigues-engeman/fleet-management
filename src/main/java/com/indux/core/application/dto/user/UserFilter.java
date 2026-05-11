package com.indux.core.application.dto.user;

import com.indux.core.domain.model.auth.UserRole;

public record UserFilter(
        UserRole role,
        Boolean isDisable
) {
}
