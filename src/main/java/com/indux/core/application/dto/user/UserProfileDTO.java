package com.indux.core.application.dto.user;

import com.mongodb.lang.Nullable;

public record UserProfileDTO(@Nullable SimpleUser usuario, @Nullable String descricao, @Nullable String instagram,
                             @Nullable String twitter,
                             @Nullable String facebook) {
}
