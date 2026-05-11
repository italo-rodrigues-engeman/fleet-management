package com.indux.modules.employee_history.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterHistory{
    Date competence;
    List<Long> regionalId;
    List<Long> contractId;
    List<Long> projectId;
    List<Integer> filialId;
    List<String> event;
    List<String> eventDescription;
    List<String> registration;
}
