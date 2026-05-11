package com.indux.modules.calibration.domain.entities.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
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

@Document(collection = "calibration_tolerance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ToleranceEntity {
    @Id
    private String id;
    
    @DBRef
    private List<PropertiesEntity> properties;
    
    @Field("branch_id")
    private List<Long> branchId;
    
    @Field("contract_id")
    private List<Long> contractId;
    
    @Field("project_id")
    private List<Long> projectId;
    
    private Double valueTolerance;

    List<OrganizationDTO> branch;
    List<ContractDTO> contract;
    List<ProjectDTO> project;
}