package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RequestResponse {

    @JsonProperty("numero_solicitacao")
    private Integer requestNumber;

    @JsonProperty("solicitante")
    private String requester;

    @JsonProperty("data_solicitacao")
    private Instant requestDate ;
}
