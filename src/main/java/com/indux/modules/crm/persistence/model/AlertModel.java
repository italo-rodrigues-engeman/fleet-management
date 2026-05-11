package com.indux.modules.crm.persistence.model;


import com.mongodb.annotations.Immutable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Document("crm_alert")
public class AlertModel {

    @Id
    private String id;

    private LocalDate date;

    @DBRef
    private EngemanAgentModel engemanAgent;

    private String futureActionDescription;

    private Boolean sent = false;

    @DBRef
    private CommercialInteractionsModel commercialInteraction;
}
