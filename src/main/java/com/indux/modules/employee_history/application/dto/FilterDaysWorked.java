package com.indux.modules.employee_history.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterDaysWorked{
    Date competence;
    List<Long> diretoriaId;
    List<Long> superintendenciaId;
    List<Long> setorId;
    List<Long> regionalId;
    List<Long> contractId;
    List<Long> projectId;
    List<Integer> filialId;
    List<Long> filialMegaId;
    List<Integer> centroCustoHcm;
    String registration;
    String situation;
    String movimentation;
    Boolean isClose;
    String projectType;
    String orderBy;
    Boolean direction;
}
