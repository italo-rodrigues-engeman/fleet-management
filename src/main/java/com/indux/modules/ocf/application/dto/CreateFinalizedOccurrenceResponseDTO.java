package com.indux.modules.ocf.application.dto;

public record CreateFinalizedOccurrenceResponseDTO(
        String message,
        int status,
        String id,
        Long codeId
) {}


