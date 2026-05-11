package com.indux.modules.training.domain.entity;

import com.indux.modules.training.application.dto.FiliaisHCMResponse;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "training")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainingEntity {
    @Id
    private String id;
    private String name;
    private Boolean status;
    private String description;
    private Double effective;
    private List<FiliaisHCMResponse> filiais;
    private List<String> prestserv;
    private List<String> myDrake;
}
