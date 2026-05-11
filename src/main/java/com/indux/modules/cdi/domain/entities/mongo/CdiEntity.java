package com.indux.modules.cdi.domain.entities.mongo;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.cdi.aplication.dtos.ApplicantDTO;
import com.indux.modules.cdi.aplication.dtos.AvaliationDTO;
import com.indux.modules.cdi.aplication.dtos.DetailsDTO;
import com.indux.modules.cdi.aplication.dtos.EspecificResultDTO;
import com.indux.modules.cdi.domain.entities.models.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "cdi_collection")
public class CdiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private long autoIncrementId;
    @JsonProperty("applicant")
    private ApplicantDTO applicant;
    @JsonProperty("type")
    private Type type;
    @JsonProperty("scope")
    private Scope scope;
    @JsonProperty("complexity")
    private Complexity complexity;
    @JsonProperty("previst_time")
    private PrevistTime  previstTime;
    @JsonProperty("description")
    private String description;
    @JsonProperty("independent")
    private Boolean independent;
    @JsonProperty("details")
    private DetailsDTO details;
    @JsonProperty("problem")
    private String problem;
    @JsonProperty("result")
    private String result;
    @JsonProperty("especific_result")
    private EspecificResultDTO  especificResult;
    @JsonProperty("status")
    private Status status;
    @JsonProperty("stage")
    private Stage stage;
    @JsonProperty("local")
    private AvaliationDTO local;
    @JsonProperty("dir1")
    private AvaliationDTO dir1;
    @JsonProperty("dir2")
    private AvaliationDTO dir2;
    @JsonProperty("create_At")
    private Date createAt;
    @JsonProperty("points")
    private Integer points;
    @JsonProperty("priorty_level")
    private Complexity priortyLevel;
    @JsonProperty("final_responsabilty")
    private ApplicantDTO finalResponsabilty;
    @JsonProperty("statusOrder")
    private Integer statusOrder;
    @JsonProperty("term")
    private Date term;
    private String title;
}
