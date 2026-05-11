package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private Integer requestNumber;
    private String requester;
    private Instant requestDate;
}
