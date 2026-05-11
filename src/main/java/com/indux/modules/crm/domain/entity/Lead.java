package com.indux.modules.crm.domain.entity;

import com.indux.modules.crm.domain.enums.DecisionMakeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    private String id;

    private String name;

    //Criar ENUM
    private String function;

    private DecisionMakeLevel decisionMakeLevel;

    private String unit;

    private String company;

    private String mainEmail;

    private String alternativeEmail;

    private String mainNumber;

    private String alternativeNumber;

    private String linkedinAccount;

    private String observations;

    private String responsibleUser;

    private Date registrationDate;

    private Boolean status;

    public Lead createGeneric() {
        Lead lead = new Lead();

        lead.setName("Lead portaria");
        lead.setFunction("OUTRO");
        lead.setDecisionMakeLevel(DecisionMakeLevel.OUTRO);
        lead.setMainEmail("generico@generico.com");
        lead.setMainNumber("00000000000");
        lead.setStatus(true);

        return lead;
    }
}
