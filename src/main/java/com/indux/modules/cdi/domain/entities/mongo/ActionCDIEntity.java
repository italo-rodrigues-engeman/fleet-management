package com.indux.modules.cdi.domain.entities.mongo;

import com.indux.modules.cdi.aplication.dtos.ApplicantDTO;
import com.indux.modules.cdi.aplication.dtos.DevelopmentDTO;
import com.indux.modules.cdi.aplication.dtos.RequestActionDTO;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "cdi_action_collection")
public class ActionCDIEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private String id;
        @Field("cdi_id")
        private String cdiId;
        private Date term;
        private ApplicantDTO evaluator;
        private List<DevelopmentDTO> development;
        private Boolean isDev;
        private Integer points;
        private RequestActionDTO request;
        private RequestActionDTO approval;
        private DevelopmentDTO test;
        @Field("final_test")
        private DevelopmentDTO finalTest;
        @Field("avaliation_description")
        private String avaliationDescription;
        private String reason;
        private List<DevelopmentDTO> fixes;
        private List<DevelopmentDTO> improves;
        private String status;
        private String title;
}
