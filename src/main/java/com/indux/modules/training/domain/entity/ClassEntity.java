package com.indux.modules.training.domain.entity;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.training.application.dto.EmployeeResponseDTO;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "training_class")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassEntity {
    @Id
    private String id;
    private Long autoId;
    private String filialHCM;
    @DBRef
    private TrainingEntity training;
    @DBRef
    private InstituinEntity instituin;
    private Date dateStrart;
    private Date dateEnd;
    private Date finished;
    private Date validity;
    private String type;
    private String modality;
    private Double capacityMax;
    private Double capacityMin;
    private String workload;
    private String observation;
    private List<EmployeeResponseDTO> collaborators;
    private Boolean status;
    private AttachmentEntity attachment;
}
