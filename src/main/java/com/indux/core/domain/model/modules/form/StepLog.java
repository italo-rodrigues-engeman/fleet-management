package com.indux.core.domain.model.modules.form;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StepLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Field(name = "name")
    private String name;
    @Field(name = "grupo")
    private UUID group;
    @Field(name = "usuario")
    private UUID user;
    @Field(name = "criado_em")
    private Date created_at;
    @Field(name = "finalizado_em")
    private Date final_at;
    @Field(name = "etapa")
    private int step;
    @Field(name = "observacao")
    private String observation;

}


