package com.indux.modules.crm.persistence.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import com.indux.modules.crm.domain.enums.DecisionMakeLevel;
import com.mongodb.annotations.Immutable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Document(collection = "crm_lead")
public class LeadModel {

    @Id
    private String id;

    private String name;

    private String function;

    private DecisionMakeLevel decisionMakeLevel;

    private String mainEmail;

    private String alternativeEmail;

    private String mainNumber;

    private String alternativeNumber;

    private String linkedinAccount;

    private String observations;

    private Boolean status;

    private String unit;

    private String company;

    private String responsibleUser;

    @CreatedDate
    private Date registrationDate;

    public void toggleStatus(){
        this.setStatus(!this.getStatus());
    }

}
