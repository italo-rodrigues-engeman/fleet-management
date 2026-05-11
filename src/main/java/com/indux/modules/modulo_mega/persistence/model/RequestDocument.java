package com.indux.modules.modulo_mega.persistence.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDocument {
    private Integer requestNumber;
    private String requester;
    private Instant requestDate;
}