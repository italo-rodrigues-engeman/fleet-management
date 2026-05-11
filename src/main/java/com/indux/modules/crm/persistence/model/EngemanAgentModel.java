package com.indux.modules.crm.persistence.model;


import com.indux.core.domain.model.modules.AttachmentEntity;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crm_engeman_agent")
public class EngemanAgentModel {

    @Id
    private String id;

    private String name;

    private String cpf;

    private String cep;

    private String address;

    private String state;

    private String city;

    private String mainEmail;

    private String alternativeEmail;

    private String mainNumber;

    private String alternativeNumber;

    private Boolean status;

    private List<AttachmentEntity> attachments;

    @DBRef
    private List<CommissionModel> commissions;

    @DBRef
    private List<CommercialInteractionsModel> commercialInteractions;


    public void toggleStatus(){
        this.setStatus(!this.getStatus());
    }
}
