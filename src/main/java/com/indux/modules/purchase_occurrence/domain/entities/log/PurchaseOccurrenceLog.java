package com.indux.modules.purchase_occurrence.domain.entities.log;

import com.indux.core.domain.model.modules.form.StepLog;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "logs_ocorrencia_compras")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PurchaseOccurrenceLog extends StepLog {
    @Field(name = "ocorrencia_id")
    private String occurrenceId;
    @Field(name = "code_id")
    private Long code;
}
