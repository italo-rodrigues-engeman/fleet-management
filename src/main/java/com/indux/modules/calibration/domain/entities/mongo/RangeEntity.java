package com.indux.modules.calibration.domain.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "calibration_range")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RangeEntity {
    @Id
    private String id;

    private String name;

    private Integer amount;

    @DBRef
    private MeasuresEntity measure;

    @DBRef
    private UnitMeasuresEntity unit;

    private Double range1;

    private Double range2;

    private List<Double> resolution;

    private List<Double> reference;
    @DBRef
    private List<ToleranceEntity> tolerance;
}