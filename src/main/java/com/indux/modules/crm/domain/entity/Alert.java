package com.indux.modules.crm.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    private String id;

    private LocalDate date;

    private EngemanAgent engemanAgent;

    private String futureActionDescription;

    @Builder.Default
    private Boolean sent = false;
}
