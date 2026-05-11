package com.indux.modules.calibration.domain.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "calibration_organization")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NiOrganizationEntity {
    @Id
    private String id;
    private Long autoIncrementId;
    private String heritage;
    @DBRef
    private CalibrationStandardEntity calibrationStandard;
    private List<Long> branchIds;
    private List<Long> contractIds;
    private List<Long> projectIds;
    private String observation;
    @DBRef
    private List<CalibrationEntity> calibration;

    @Field("next_calibration")
    private LocalDate nextCalibration;
    @Field("assosiation_date")
    private LocalDate assosiationDate;
}
