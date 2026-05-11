package com.indux.modules.calibration.domain.entities.mongo;

//todo: corrigir dtos que são entidades. Domínio deve ser puro
import com.indux.modules.calibration.aplication.dtos.DataLog;
import com.indux.modules.calibration.aplication.dtos.ModelReturn;
import com.indux.modules.calibration.aplication.dtos.Periodicity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "calibration_standard")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalibrationStandardEntity {
    @Id
    private String id;

    private Long autoIncrementId;

    @DBRef
    private PropertiesEntity properties;

    @DBRef
    private EquipamentEntity equipament;

    private List<ModelReturn> model;

    @DBRef
    private List<RangeEntity> range;

    private String messageApproval;

    private String messageDisapproval;

    private Boolean status;

    private List<DataLog> dataLog;
}