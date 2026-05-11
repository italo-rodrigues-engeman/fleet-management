package com.indux.modules.calibration.domain.entities.mongo;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.calibration.aplication.dtos.CalibrationData;
import com.indux.modules.calibration.aplication.dtos.DataLog;
import com.indux.modules.calibration.aplication.dtos.Periodicity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "calibration")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalibrationEntity {
    @Id
    private String id;

    private LocalDate calibrationDate;
    private String standardLab;
    private String nCertification;
    private Periodicity periodicity;
    private List<AttachmentEntity> certification;
    private List<AttachmentEntity> certificationStandard;
    private List<CalibrationData> calibrationData;
    private Boolean situation;
    private List<AttachmentEntity> picture;
    private String description;
    private String parts;
    private String condition;
    private String status;
    private Boolean removed;
    private List<DataLog> dataLogs;
}
