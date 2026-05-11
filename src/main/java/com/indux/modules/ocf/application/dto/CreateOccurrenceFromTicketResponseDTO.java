package com.indux.modules.ocf.application.dto;

public record CreateOccurrenceFromTicketResponseDTO(
        String message,
        int status,
        Long timeId
) {}

