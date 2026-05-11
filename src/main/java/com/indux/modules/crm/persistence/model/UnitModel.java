package com.indux.modules.crm.persistence.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import com.indux.modules.crm.domain.enums.UnitType;
import com.mongodb.annotations.Immutable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crm_unit")
public class UnitModel {

    @Id
    private String id;

    private String type;

    private String name;

    private String cep;

    private String address;

    private String state;

    private String city;

    private String googleMapsLink;

    private String number;

    private String observations;

    private String responsibleUser;

    private Boolean status;

    private String company;

    @CreatedDate
    private Date registrationDate;

    @DBRef
    private List<LeadModel> leads;

    @DBRef
    private List<CommercialInteractionsModel> commercialInteractionsList;

    public void toggleStatus(){
        this.setStatus(!this.getStatus());
    }
}