package com.indux.modules.crm.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.enums.Market;
import com.indux.modules.crm.domain.enums.Modality;
import com.indux.modules.crm.domain.enums.RegistrationCondition;
import com.indux.modules.crm.domain.enums.Sector;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Company {

    private String id;

    private String cnpj;

    private String name;

    private Market market;

    private Sector sector;

    private Modality modality;

    private String portalUrl;

    private String portalUser;

    private String portalPassword;

    private String details;

    private RegistrationCondition registrationCondition;

    private Boolean status;

    private String responsibleUser;

    private Date registrationDate;

    private List<Unit> units = new ArrayList<>();

    private List<Lead> leads;

    private List<CommercialInteractions> commercialInteractions;

    private List<Budget> budgets;
}
