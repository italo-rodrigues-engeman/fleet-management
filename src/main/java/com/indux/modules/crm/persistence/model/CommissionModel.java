package com.indux.modules.crm.persistence.model;


import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.mongodb.annotations.Immutable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Document(collection = "crm_commission")
public class CommissionModel {

    @Id
    private String id;

    private BigDecimal initialValue;

    private BigDecimal finalValue;

    private Double percentage;

    @DBRef
    private EngemanAgentModel engemanAgent;
}
