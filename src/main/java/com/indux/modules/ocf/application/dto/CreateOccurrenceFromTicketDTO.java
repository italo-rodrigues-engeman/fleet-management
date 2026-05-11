package com.indux.modules.ocf.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ocf.domain.model.Origem;

import java.time.LocalDate;
import java.util.Optional;

public record CreateOccurrenceFromTicketDTO(
        @JsonProperty("id_ocorrencia")
        Long id_ocorrencia,  // ID da ocorrência para buscar o ticket (bigint)
        @JsonProperty("origem")
        Origem origem,
        @JsonProperty("dataInicial")
        Optional<LocalDate> dataInicial,
        @JsonProperty("prioridade")
        Optional<String> prioridade,
        @JsonProperty("telefoneDeContato")
        Optional<String> telefoneDeContato
) {
    public CreateOccurrenceFromTicketDTO {
        // Constructor validation
        if (id_ocorrencia == null) {
            throw new IllegalArgumentException("id_ocorrencia não pode ser null");
        }
        if (origem == null) {
            throw new IllegalArgumentException("origem não pode ser null");
        }
    }
} 