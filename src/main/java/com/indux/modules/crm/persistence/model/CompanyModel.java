package com.indux.modules.crm.persistence.model;

import com.indux.modules.crm.domain.enums.Market;
import com.indux.modules.crm.domain.enums.Modality;
import com.indux.modules.crm.domain.enums.RegistrationCondition;
import com.indux.modules.crm.domain.enums.Sector;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crm_company")
public class CompanyModel {

    @Id
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

    @CreatedDate
    private Date registrationDate;

    @DBRef
    private List<UnitModel> units;

    @DBRef
    private List<LeadModel> leads;

    @DBRef
    private List<CommercialInteractionsModel> commercialInteractions;

    @DBRef
    private List<BudgetModel> budgets;

    public void toggleStatus(){
        this.setStatus(!this.getStatus());
    }
}