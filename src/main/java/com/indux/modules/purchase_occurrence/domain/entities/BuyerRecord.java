package com.indux.modules.purchase_occurrence.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record BuyerRecord(
        @JsonProperty("matricula") String registration,
        @JsonProperty("nome") String name,
        UUID id
    ){
}
