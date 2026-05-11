package com.indux.modules.crm.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.enums.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    private LocalDate date;

    private String orderNumber;

    private String description;

    private BigDecimal value;

    private BudgetStatus status;
}
