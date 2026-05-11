package com.indux.modules.budgets.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "simple_budget_item_groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleBudgetItemGroup {
    @Id
    private String id; // MongoDB default ID
    
    @Indexed(unique = true)
    private Integer sequentialId; // Our sequential ID
    
    private String name;
    private String description;
}
