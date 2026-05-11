package com.indux.core.application.dto.auth;

import java.util.List;

public record RegisterBatchResponse(
        List<CreatedUserItem> created,
        List<SkippedUserItem> skipped
) {
}
