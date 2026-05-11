package com.indux.modules.ocf.application.dto;

public record CreateOccurrenceResponseDTO(
    String message,
    int status,
    String occurrenceId,
    Long codeID
) {
    public static CreateOccurrenceResponseDTO success(String message, String occurrenceId, Long codeID) {
        return new CreateOccurrenceResponseDTO(message, 201, occurrenceId, codeID);
    }
}

