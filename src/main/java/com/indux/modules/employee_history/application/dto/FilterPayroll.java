package com.indux.modules.employee_history.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterPayroll {
    private Date competenceStart;
    private Date competenceEnd;
    private String registration;
    private String eventName;
    private Double minValue;
    private Double maxValue;
    private String eventDescription;
    private List<Long> filialHcm;
    private String employeeName;
    private String status;
}
