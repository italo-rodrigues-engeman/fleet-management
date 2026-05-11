package com.indux.modules.organization_chart.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProjectFilter{
    private String search;
    private Boolean ativo;
    private Integer mega;
    private Integer hcm;
    private Long subordinate;
    private List<Long> organization;
    private List<Long> contracts;
    private List<Integer> filial;
    private List<Long> regional;
}
