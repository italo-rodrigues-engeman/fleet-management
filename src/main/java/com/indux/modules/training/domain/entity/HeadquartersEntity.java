package com.indux.modules.training.domain.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "training_headquarters")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HeadquartersEntity {
    @Id
    private String id;
    private String name;
    private String ac;
    private String filialHCM;
    private String client;
    private String environment;
}
