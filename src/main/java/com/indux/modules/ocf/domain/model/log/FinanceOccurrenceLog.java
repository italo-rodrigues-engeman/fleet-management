package com.indux.modules.ocf.domain.model.log;

import com.indux.core.domain.model.modules.form.StepLog;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "logs_ocorrencia_ff")
@AllArgsConstructor
@NoArgsConstructor
public class FinanceOccurrenceLog extends StepLog {
    @Field(name = "ocorrencia_id")
    private String occurrenceId;
    @Field(name = "code_id")
    private Long code;

}
