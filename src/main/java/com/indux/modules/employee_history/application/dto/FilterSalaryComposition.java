package com.indux.modules.employee_history.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterSalaryComposition {
    private List<String> registrations;
    private List<String> filialIds;
    private String employeeName;
    private String status;
    private Date competence;
}
