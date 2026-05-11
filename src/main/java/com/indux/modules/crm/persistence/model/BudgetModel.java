package com.indux.modules.crm.persistence.model;


import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;
import com.indux.modules.crm.domain.enums.BudgetStatus;
import com.mongodb.annotations.Immutable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Document(collection = "crm_budget")
public class BudgetModel {

    @Id
    private String id;

    private LocalDate date;

    private String orderNumber;

    private String description;

    private BigDecimal value;

    private BudgetStatus status;

    @DBRef
    private CompanyModel company;
}
