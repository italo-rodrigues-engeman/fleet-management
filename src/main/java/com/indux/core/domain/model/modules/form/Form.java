package com.indux.core.domain.model.modules.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Form<T> {
    @Field(name = "contrato_id")
    @Deprecated
    private Integer contratoId;
    @Field(name = "regionalId")
    private Integer regionalId;
    @Field(name = "regionalNome")
    private String regionalNome;
    @Field(name = "projectId")
    private Integer projectId;
    @Field(name = "projectName")
    private String projectName;
    @Field(name = "etapa_atual")
    private int currentStep;
    @Field(name = "criado_em")
    @CreatedDate
    private Date created_at;
    @Field(name = "finalizado_em")
    private Date final_date;
    @Field(name = "status")
    private DocumentStatus status;
    @Field(name = "etapa_log")
    private List<StepLog> stepLog;
    @Field(name = "codigo")
    private Long codeID;
    @Field(name = "situacao")
    private DocumentStatus situacao;
    @JsonIgnore
    @Field("data_log")
    private Date dataLog;
    @Field("statusOrder")
    @Indexed(name = "statusOrder_1")
    @JsonIgnore
    private Integer statusOrder;
    @Field("situacaoOrder")
    @Indexed(name = "situacaoOrder_1")
    @JsonIgnore
    private Integer situacaoOrder;
}
