package com.indux.modules.training.domain.entity;


import com.indux.modules.training.application.dto.ProposalResponseDTO;
import com.indux.modules.training.application.dto.UnitDTO;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "training_instituin")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstituinEntity {
    @Id
    private String id;
    private String name;
    private  String cnpj;
    private List<UnitDTO> units;
    @DBRef
    private List<TrainingEntity> trainings;
    private List<ProposalResponseDTO> proposals;
}
